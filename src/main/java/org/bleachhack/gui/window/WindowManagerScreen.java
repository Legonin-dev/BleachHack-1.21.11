/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui.window;

import java.util.List;
import org.apache.commons.lang3.tuple.Triple;
import org.bleachhack.gui.window.widget.WindowButtonWidget;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;


public class WindowManagerScreen extends WindowScreen {

	/** [Window Screen, Name, Icon] **/
	public Triple<WindowScreen, String, ItemStack>[] windows;
	private int selected;

	@SafeVarargs
	public WindowManagerScreen(Triple<WindowScreen, String, ItemStack>... windows) {
		super(Text.empty(), false);
		this.windows = windows;
	}

	@Override
	public void init() {
		super.init();
		selectWindow(selected);

		int x = 1;
		int size = Math.min(width / windows.length - 1, 90);
		for (int i = 0; i < windows.length; i++) {
			int fi = i;
			addGlobalWidget(new WindowTabButtonWidget(x, height - 15, x + size, height - 1,
					windows[i].getMiddle(), windows[i].getRight(), () -> selectWindow(fi)));
			x += size + 1;
		}
	}

	@SuppressWarnings("unchecked")
	public void selectWindow(int s) {
		selected = s;
		for (Triple<WindowScreen, String, ItemStack> t: windows) {
			remove(t.getLeft());
		}

		getSelectedScreen().init(width, height - 16);
		addDrawable(getSelectedScreen());
		((List<Element>) children()).add(getSelectedScreen());
	}

	public WindowScreen getSelectedScreen() {
		return windows[selected].getLeft();
	}

	public String getSelectedTitle() {
		return windows[selected].getMiddle();
	}

	public ItemStack getSelectedIcon() {
		return windows[selected].getRight();
	}

	// Children don't tick brue
	@Override
	public void tick() {
		getSelectedScreen().tick();
		super.tick();
	}

	// Children don't get their mouse release/drag events when the click wasn't consumed by them
	@Override
	public boolean mouseReleased(Click click) {
		boolean childDragging = isDragging();
		boolean handled = super.mouseReleased(click);

		if (!childDragging) {
			handled |= getSelectedScreen().mouseReleased(click);
		}

		return handled;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return getSelectedScreen().mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
				|| super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	// Children also don't take keyboard input brueh
	@Override
	public boolean keyPressed(KeyInput input) {
		getSelectedScreen().keyPressed(input);
		return super.keyPressed(input);
	}

	@Override
	public boolean keyReleased(KeyInput input) {
		getSelectedScreen().keyReleased(input);
		return super.keyReleased(input);
	}

	@Override
	public boolean charTyped(CharInput input) {
		getSelectedScreen().charTyped(input);
		return super.charTyped(input);
	}

	private static class WindowTabButtonWidget extends WindowButtonWidget {

		private ItemStack item;

		public WindowTabButtonWidget(int x1, int y1, int x2, int y2, String text, ItemStack item, Runnable action) {
			super(x1, y1, x2, y2, 0xff6060b0, 0xff8070b0, 0x40606090, 0x4fb070f0, text, action);
			this.item = item;
		}

		@Override
		public void render(DrawContext ctx, int windowX, int windowY, int mouseX, int mouseY) {
			int bx1 = windowX + x1;
			int by1 = windowY + y1;
			int bx2 = windowX + x2;
			int by2 = windowY + y2;

			Window.fill(ctx,
					bx1, by1, bx2, by2,
					colorTop, colorBottom,
					isInBounds(windowX, windowY, mouseX, mouseY) ? colorHoverFill : colorFill);

			ctx.getMatrices().pushMatrix();
			ctx.getMatrices().translate((float) (bx1 + 2), (float) (by1 - 6 + (by2 - by1) / 2.0));
			ctx.getMatrices().scale(0.7f, 0.7f);

			ctx.drawItem(item, 0, 0);

			ctx.getMatrices().popMatrix();

			ctx.drawTextWithShadow(mc.textRenderer, text, bx1 + 16, by1 + (by2 - by1) / 2 - 4, -1);
		}
	}
}
