/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.bleachhack.event.events.EventRenderTooltip;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.ItemContentUtils;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Peek extends Module {

	private static final Identifier MAP_BACKGROUND_CHECKERBOARD = Identifier.of("textures/map/map_background_checkerboard.png");

	private List<List<String>> pages;
	private int slotX = -1;
	private int slotY = -1;
	private int pageCount = 0;
	private boolean shown = false;

	public Peek() {
		super("Peek", KEY_UNBOUND, ModuleCategory.MISC, "Shows whats inside containers.",
				new SettingToggle("Containers", true).withDesc("Shows a tooltip for containers.").withChildren(
						new SettingMode("Info", "All", "Name", "None").withDesc("How to show the old tooltip.")),
				new SettingToggle("Books", true).withDesc("Show tooltips for books."),
				new SettingToggle("Maps", true).withDesc("Show tooltips for maps.").withChildren(
						new SettingSlider("Map Size", 0.25, 1.5, 0.85, 2).withDesc("How big to make the map.")));
	}

	@BleachSubscribe
	public void drawScreen(EventRenderTooltip event) {
		if (!(event.getScreen() instanceof HandledScreen)) {
			return;
		}

		Slot slot = ((HandledScreen<?>) event.getScreen()).focusedSlot;
		if (slot == null)
			return;

		if (slot.x != slotX || slot.y != slotY) {
			pageCount = 0;
			pages = null;

			slotX = slot.x;
			slotY = slot.y;
		}

		DrawContext ctx = event.getContext();
		ctx.getMatrices().pushMatrix();
		ctx.getMatrices().translate(0, 0);

		if (getSetting(0).asToggle().getState()) {
			List<TooltipComponent> components = drawShulkerToolTip(ctx, slot, event.getMouseX(), event.getMouseY());
			if (components != null) {
				if (components.isEmpty()) {
					event.setCancelled(true);
				} else {
					event.setComponents(components);
				}
			}
		}

		if (getSetting(1).asToggle().getState()) drawBookToolTip(ctx, slot, event.getMouseX(), event.getMouseY());
		if (getSetting(2).asToggle().getState()) drawMapToolTip(ctx, slot, event.getMouseX(), event.getMouseY());

		ctx.getMatrices().popMatrix();
	}

	public List<TooltipComponent> drawShulkerToolTip(DrawContext ctx, Slot slot, int mouseX, int mouseY) {
		if (!(slot.getStack().getItem() instanceof BlockItem)) {
			return null;
		}

		Block block = ((BlockItem) slot.getStack().getItem()).getBlock();

		if (!(block instanceof ShulkerBoxBlock)
				&& !(block instanceof ChestBlock)
				&& !(block instanceof BarrelBlock)
				&& !(block instanceof DispenserBlock)
				&& !(block instanceof HopperBlock)
				&& !(block instanceof AbstractFurnaceBlock)) {
			return null;
		}

		List<ItemStack> items = ItemContentUtils.getItemsInContainer(slot.getStack());

		if (items.stream().allMatch(ItemStack::isEmpty)) {
			return null;
		}

		int mode = getSetting(0).asToggle().getChild(0).asMode().getMode();
		int realY = mode == 2 ? mouseY + 24 : mouseY;
		int tooltipWidth = block instanceof AbstractFurnaceBlock ? 47 : block instanceof HopperBlock ? 82 : 150;
		int tooltipHeight = block instanceof AbstractFurnaceBlock || block instanceof HopperBlock || block instanceof DispenserBlock ? 13 : 47;

		renderTooltipBox(ctx, mouseX, realY - tooltipHeight - 7, tooltipWidth, tooltipHeight, true);

		int count = block instanceof HopperBlock || block instanceof DispenserBlock || block instanceof AbstractFurnaceBlock ? 18 : 0;

		for (ItemStack i : items) {
			if (count > 26) {
				break;
			}

			int x = mouseX + 11 + 17 * (count % 9);
			int y = realY - 67 + 17 * (count / 9);

			ctx.drawItem(i, x, y);
			ctx.drawStackOverlay(mc.textRenderer, i, x, y);
			count++;
		}

		if (mode == 1) {
			return Arrays.asList(TooltipComponent.of(slot.getStack().getName().asOrderedText()));
		} else if (mode == 2) {
			return List.of();
		}

		return null;
	}

	public void drawBookToolTip(DrawContext ctx, Slot slot, int mouseX, int mouseY) {
		if (slot.getStack().getItem() != Items.WRITABLE_BOOK && slot.getStack().getItem() != Items.WRITTEN_BOOK)
			return;

		if (pages == null) {
			pages = ItemContentUtils.getTextInBook(slot.getStack());
		}

		if (pages.isEmpty()) {
			return;
		}

		/* Cycle through pages */
		if (mc.player.age % 80 == 0 && !shown) {
			shown = true;
			if (pageCount == pages.size() - 1) {
				pageCount = 0;
			} else {
				pageCount++;
			}
		} else if (mc.player.age % 80 != 0) {
			shown = false;
		}

		ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BookScreen.BOOK_TEXTURE, mouseX, mouseY - 143, 134, 134);

		Text pageIndexText = Text.translatable("book.pageIndicator", pageCount + 1, pages.size());
		int pageIndexLength = mc.textRenderer.getWidth(pageIndexText);

		ctx.getMatrices().pushMatrix();
		ctx.getMatrices().scale(0.7f, 0.7f);

		ctx.drawText(mc.textRenderer, pageIndexText,
				(int) ((mouseX + 123 - pageIndexLength) * 1.43f),
				(int) ((mouseY - 133) * 1.43f),0xFF000000, false);


		int count = 0;
		for (String s : pages.get(pageCount)) {
			ctx.drawText(mc.textRenderer, s,
					(int) ((mouseX + 24) * 1.43f),
					(int) ((mouseY - 123 + count * 7) * 1.43f),0xFF000000, false);

			count++;
		}

		ctx.getMatrices().popMatrix();

	}

	public void drawMapToolTip(DrawContext ctx, Slot slot, int mouseX, int mouseY) {
		if (slot.getStack().getItem() != Items.FILLED_MAP) {
			return;
		}

		MapState mapState = FilledMapItem.getMapState(slot.getStack(), mc.world);

		if (mapState == null) {
			return;
		}

		float scale = getSetting(2).asToggle().getChild(0).asSlider().getValueFloat() / 1.25f;

		ctx.getMatrices().pushMatrix();
		ctx.getMatrices().translate(mouseX + 14, mouseY - 18 - 135 * scale);
		ctx.getMatrices().scale(scale, scale);

		ctx.drawGuiTexture(RenderPipelines.GUI_TEXTURED, MAP_BACKGROUND_CHECKERBOARD, -7, -7, 142, 142);

		MapRenderState state = new MapRenderState();
		mc.getMapRenderer().update(slot.getStack().get(DataComponentTypes.MAP_ID), mapState, state);
		ctx.drawMap(state);

		ctx.getMatrices().popMatrix();
	}

	private void renderTooltipBox(DrawContext ctx, int x1, int y1, int x2, int y2, boolean wrap) {
		int xStart = x1 + 12;
		int yStart = y1 - 12;
		if (wrap) {
			if (xStart + x2 > mc.currentScreen.width)
				xStart -= 28 + x2;
			if (yStart + y2 + 6 > mc.currentScreen.height)
				yStart = mc.currentScreen.height - y2 - 6;
		}

		int background = 0xF0100010;
		int borderStart = 0x505000FF;
		int borderEnd = 0x5028007F;

		ctx.fill(xStart - 3, yStart - 4, xStart + x2 + 3, yStart - 3, background);
		ctx.fill(xStart - 3, yStart + y2 + 3, xStart + x2 + 3, yStart + y2 + 4, background);
		ctx.fill(xStart - 3, yStart - 3, xStart + x2 + 3, yStart + y2 + 3, background);
		ctx.fill(xStart - 4, yStart - 3, xStart - 3, yStart + y2 + 3, background);
		ctx.fill(xStart + x2 + 3, yStart - 3, xStart + x2 + 4, yStart + y2 + 3, background);

		ctx.fillGradient(xStart - 3, yStart - 3 + 1, xStart - 3 + 1, yStart + y2 + 3 - 1, borderStart, borderEnd);
		ctx.fillGradient(xStart + x2 + 2, yStart - 3 + 1, xStart + x2 + 3, yStart + y2 + 3 - 1, borderStart, borderEnd);
		ctx.fillGradient(xStart - 3, yStart - 3, xStart + x2 + 3, yStart - 3 + 1, borderStart, borderStart);
		ctx.fillGradient(xStart - 3, yStart + y2 + 2, xStart + x2 + 3, yStart + y2 + 3, borderEnd, borderEnd);
	}
}
