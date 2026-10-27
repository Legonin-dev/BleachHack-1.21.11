/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.setting.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;

import net.minecraft.text.Text;

import org.bleachhack.gui.clickgui.window.ModuleWindow;
import org.bleachhack.gui.window.Window;
import org.bleachhack.gui.window.WindowScreen;
import org.bleachhack.gui.window.widget.WindowButtonWidget;
import org.bleachhack.gui.window.widget.WindowScrollbarWidget;
import org.bleachhack.gui.window.widget.WindowTextFieldWidget;
import org.bleachhack.setting.SettingDataHandler;
import org.bleachhack.setting.SettingDataHandlers;
import org.bleachhack.util.io.BleachFileHelper;

import java.util.*;

public abstract class SettingList<T> extends ModuleSetting<LinkedHashSet<T>> {

	protected String windowText;
	protected Set<T> itemPool;

	@SuppressWarnings("unchecked")
	public SettingList(String text, String windowText, SettingDataHandler<T> itemHandler, Collection<T> itemPool, T... defaultItems) {
		super(text, new LinkedHashSet<>(Arrays.asList(defaultItems)), v -> (LinkedHashSet<T>) v.clone(), SettingDataHandlers.ofCollection(itemHandler, LinkedHashSet::new));
		this.windowText = windowText;
		this.itemPool = new LinkedHashSet<>(itemPool);
	}

	public void render(ModuleWindow window, DrawContext ctx, int x, int y, int len) {
		if (window.mouseOver(x, y, x + len, y + 12)) {
			ctx.fill(x + 1, y, x + len, y + 12, 0x70303070);
		}

		ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, getName(), x + 3, y + 2, 0xFFcfe0cf);

		ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "...", x + len - 7, y + 2, 0xFFcfd0cf);

		if (window.mouseOver(x, y, x + len, y + 12) && window.lmDown) {
			window.mouseReleased(window.mouseX, window.mouseY, 1);
			MinecraftClient.getInstance().currentScreen.mouseReleased(new net.minecraft.client.gui.Click(window.mouseX, window.mouseY, new net.minecraft.client.input.MouseInput(0, 0)));
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.3F));
			MinecraftClient.getInstance().setScreen(new ListWidowScreen(MinecraftClient.getInstance().currentScreen));
		}
	}

	public boolean contains(T item) {
		return getValue().contains(item);
	}

	public void renderItem(MinecraftClient mc, DrawContext ctx, T item, int x, int y, int w, int h) {
		ctx.getMatrices().pushMatrix();

		float scale = (h - 2) / 10f;
		float offset = 1f / scale;

		ctx.getMatrices().scale(scale, scale);

		ctx.drawTextWithShadow(mc.textRenderer, "?", (int) ((x + 5) * offset), (int) ((y + 4) * offset), -1);

		ctx.getMatrices().popMatrix();
	}

	/**
	 * The human readable name for this item, the internal name is used for read/writing.
	 */
	public abstract Text getName(T item);

	public SettingList<T> withDesc(String desc) {
		setTooltip(desc);
		return this;
	}

	public int getHeight(int len) {
		return 12;
	}

	private class ListWidowScreen extends WindowScreen {

		private Screen parent;
		private WindowTextFieldWidget inputField;
		private WindowScrollbarWidget scrollbar;

		private T toDeleteItem;
		private T toAddItem;

		public ListWidowScreen(Screen parent) {
			super(Text.literal(windowText));
			this.parent = parent;
		}

		public void init() {
			super.init();

			clearWindows();

			addWindow(new Window(
					(int) (width / 3.25),
					height / 12,
					(int) (width - width / 3.25),
					height - height / 12,
					windowText, new ItemStack(Items.OAK_SIGN)));

			int x2 = getWindow(0).x2 - getWindow(0).x1;
			int y2 = getWindow(0).y2 - getWindow(0).y1;

			getWindow(0).addWidget(new WindowButtonWidget(x2 - 50, y2 - 22, x2 - 5, y2 - 5, "Reset", () -> {
				getValue().clear();
				getValue().addAll(defaultValue);
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
			}));

			getWindow(0).addWidget(new WindowButtonWidget(x2 - 100, y2 - 22, x2 - 55, y2 - 5, "Clear", () -> {
				getValue().clear();
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
			}));

			getWindow(0).addWidget(new WindowButtonWidget(x2 - 150, y2 - 22, x2 - 105, y2 - 5, "Add All", () -> {
				getValue().clear();
				getValue().addAll(itemPool);
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
			}));

			inputField = getWindow(0).addWidget(new WindowTextFieldWidget(5, y2 - 22, x2 / 3, 17, inputField != null ? inputField.textField.getText() : ""));

			scrollbar = getWindow(0).addWidget(new WindowScrollbarWidget(x2 - 11, 12, 0, y2 - 39, scrollbar == null ? 0 : scrollbar.getPageOffset()));
		}

		public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
			renderBackground(ctx, mouseX, mouseY, delta);
			super.render(ctx, mouseX, mouseY, delta);
		}

		public void onRenderWindow(DrawContext ctx, int window, int mouseX, int mouseY) {
			super.onRenderWindow(ctx, window, mouseX, mouseY);

			toAddItem = null;
			toDeleteItem = null;

			if (window == 0) {
				int x1 = getWindow(0).x1;
				int y1 = getWindow(0).y1;
				int x2 = getWindow(0).x2;
				int y2 = getWindow(0).y2;

				int maxEntries = Math.max(1, (y2 - y1) / 21 - 1);
				int renderEntries = 0;
				int entries = 0;

				scrollbar.setTotalHeight(getValue().size() * 21);
				int offset = scrollbar.getPageOffset();

				for (T e: getValue()) {
					if (entries >= offset / 21 && renderEntries < maxEntries) {
						drawEntry(ctx, e, x1 + 6, y1 + 15 + entries * 21 - offset, x2 - x1 - 19, 20, mouseX, mouseY);
						renderEntries++;
					}

					entries++;
				}

				//Window.horizontalGradient(matrix, x1 + 1, y2 - 25, x2 - 1, y2 - 1, 0x70606090, 0x00606090);
				Window.horizontalGradient(ctx, x1 + 1, y2 - 27, x2 - 1, y2 - 26, 0xff606090, 0x50606090);

				if (inputField.textField.isFocused()) {
					Set<T> toDraw = new LinkedHashSet<>();

					for (T e: itemPool) {
						if (toDraw.size() >= 10)
							break;

						if (!getValue().contains(e) && getName(e).getString().toLowerCase(Locale.ENGLISH).contains(inputField.textField.getText().toLowerCase(Locale.ENGLISH))) {
							toDraw.add(e);
						}
					}

					int curY = y1 + inputField.y1 - 4 - toDraw.size() * 17;
					int longest = toDraw.stream().mapToInt(e -> textRenderer.getWidth(getName(e))).max().orElse(0);

					ctx.getMatrices().pushMatrix();

					for (T e: toDraw) {
						drawSearchEntry(ctx, e, x1 + inputField.x1, curY, longest + 23, 16, mouseX, mouseY);
						curY += 17;
					}

					ctx.getMatrices().popMatrix();
				}
			}
		}

		private void drawEntry(DrawContext ctx, T item, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean mouseOverDelete = mouseX >= x + width - 14 && mouseX <= x + width - 1 && mouseY >= y + 2 && mouseY <= y + height - 2;
			Window.fill(ctx, x + width - 14, y + 2, x + width - 1, y + height - 2, mouseOverDelete ? 0x4fb070f0 : 0x60606090);

			if (mouseOverDelete) {
				toDeleteItem = item;
			}

			renderItem(client, ctx, item, x, y, height, height);

			ctx.drawTextWithShadow(textRenderer, getName(item), x + height + 4, y + 4, -1);
			ctx.drawTextWithShadow(textRenderer, "\u00a7cx", x + width - 10, y + 5, -1);
		}

		private void drawSearchEntry(DrawContext ctx, T item, int x, int y, int width, int height, int mouseX, int mouseY) {
			boolean mouseOver = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
			ctx.fill(x, y - 1, x + width, y + height, mouseOver ? 0xdf8070d0 : 0xb0606090);

			if (mouseOver) {
				toAddItem = item;
			}

			renderItem(client, ctx, item, x, y, height, height);

			ctx.drawTextWithShadow(textRenderer, getName(item), x + height + 4, y + 4, -1);
		}

		@Override
		public void close() {
			this.client.setScreen(parent);
		}

		@Override
		public boolean shouldPause() {
			return false;
		}

		public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
			double mouseX = click.x();
			double mouseY = click.y();
			int button = click.button();
			if (toAddItem != null) {
				getValue().add(toAddItem);
				inputField.textField.setFocused(true);
				client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.3F));
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
				return false;
			} else if (toDeleteItem != null) {
				getValue().remove(toDeleteItem);
				client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.3F));
				BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
			}

			return super.mouseClicked(click, doubled);
		}

		public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
			if (!inputField.textField.isFocused() || inputField.textField.getText().isEmpty()) {
				scrollbar.scroll(verticalAmount);
			}

			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
	}
}
