/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui.window;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.bleachhack.gui.window.widget.WindowWidget;
import org.bleachhack.util.render.RenderUtils;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Window {

	public int x1;
	public int y1;
	public int x2;
	public int y2;

	public String title;
	public ItemStack icon;

	public boolean closed;
	public boolean selected = false;

	private List<WindowWidget> widgets = new ArrayList<>();

	protected boolean dragging;
	protected int dragOffX;
	protected int dragOffY;

	public Window(int x1, int y1, int x2, int y2, String title, ItemStack icon) {
		this(x1, y1, x2, y2, title, icon, false);
	}

	public Window(int x1, int y1, int x2, int y2, String title, ItemStack icon, boolean closed) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.title = title;
		this.icon = icon;
		this.closed = closed;
	}

	public List<WindowWidget> getWidgets() {
		return widgets;
	}

	public <T extends WindowWidget> T addWidget(T widget) {
		widgets.add(widget);
		return widget;
	}

	public void render(DrawContext ctx, int mouseX, int mouseY) {
		TextRenderer textRend = MinecraftClient.getInstance().textRenderer;

		checkMouseRelease();

		if (dragging) {
			x2 = (x2 - x1) + mouseX - dragOffX - Math.min(0, mouseX - dragOffX);
			y2 = (y2 - y1) + mouseY - dragOffY - Math.min(0, mouseY - dragOffY);
			x1 = Math.max(0, mouseX - dragOffX);
			y1 = Math.max(0, mouseY - dragOffY);
		}

		drawBackground(ctx, mouseX, mouseY, textRend);

		for (WindowWidget w : widgets) {
			if (w.shouldRender(x1, y1, x2, y2)) {
				w.render(ctx, x1, y1, mouseX, mouseY);
			}
		}

		boolean blockItem = icon != null && icon.getItem() instanceof BlockItem;

		/* window icon */
		if (icon != null) {
			ctx.getMatrices().pushMatrix();
			ctx.getMatrices().translate(x1 + (blockItem ? 3 : 2), y1 + 2);
			ctx.getMatrices().scale(0.6f, 0.6f);

			ctx.drawItem(icon, 0, 0);

			ctx.getMatrices().popMatrix();
		}

		/* window title */
		ctx.drawTextWithShadow(textRend, title,
				x1 + (icon == null || icon.getItem() == Items.AIR ? 4 : (blockItem ? 15 : 14)), y1 + 3, -1);
	}

	protected void drawBackground(DrawContext ctx, int mouseX, int mouseY, TextRenderer textRend) {
		/* background */
		ctx.fill(x1, y1 + 1, x1 + 1, y2 - 1, 0xff6060b0);
		horizontalGradient(ctx, x1 + 1, y1, x2 - 1, y1 + 1, 0xff6060b0, 0xff8070b0);
		ctx.fill(x2 - 1, y1 + 1, x2, y2 - 1, 0xff8070b0);
		horizontalGradient(ctx, x1 + 1, y2 - 1, x2 - 1, y2, 0xff6060b0, 0xff8070b0);

		ctx.fill(x1 + 1, y1 + 12, x2 - 1, y2 - 1, 0x90606090);

		/* title bar */
		horizontalGradient(ctx, x1 + 1, y1 + 1, x2 - 1, y1 + 12, (selected ? 0xff6060b0 : 0xff606060), (selected ? 0xff8070b0 : 0xffa0a0a0));

		/* buttons */
		ctx.drawText(textRend, "x", x2 - 10, y1 + 3, RenderUtils.fixColor(0), false);
		ctx.drawText(textRend, "x", x2 - 11, y1 + 2, -1, false);

		ctx.drawText(textRend, "_", x2 - 21, y1 + 2, RenderUtils.fixColor(0), false);
		ctx.drawText(textRend, "_", x2 - 22, y1 + 1, -1, false);
	}

	public boolean shouldClose(int mouseX, int mouseY) {
		return selected && mouseX > x2 - 23 && mouseX < x2 && mouseY > y1 + 2 && mouseY < y1 + 12;
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX >= x1 && mouseX <= x2 - 2 && mouseY >= y1 && mouseY <= y1 + 11) {
			dragging = true;
			dragOffX = (int) mouseX - x1;
			dragOffY = (int) mouseY - y1;
		}

		if (selected) {
			try {
				for (WindowWidget w : widgets) {
					if (w.shouldRender(x1, y1, x2, y2)) {
						w.mouseClicked(x1, y1, (int) mouseX, (int) mouseY, button);
					}
				}
			} catch (ConcurrentModificationException ignored) {}
		}
	}

	public void mouseReleased(double mouseX, double mouseY, int button) {
		dragging = false;

		if (selected) {
			for (WindowWidget w : widgets) {
				if (w.shouldRender(x1, y1, x2, y2)) {
					w.mouseReleased(x1, y1, (int) mouseX, (int) mouseY, button);
				}
			}
		}
	}

	public void tick() {
		for (WindowWidget w : widgets) {
			w.tick();
		}
	}

	public void charTyped(char chr, int modifiers) {
		if (selected) {
			for (WindowWidget w : widgets) {
				w.charTyped(chr, modifiers);
			}
		}
	}

	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (selected) {
			for (WindowWidget w : widgets) {
				w.keyPressed(keyCode, scanCode, modifiers);
			}
		}
	}

	public static void fill(DrawContext ctx, int x1, int y1, int x2, int y2) {
		fill(ctx, x1, y1, x2, y2, 0xff6060b0, 0xff8070b0, 0x00000000);
	}

	public static void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int fill) {
		fill(ctx, x1, y1, x2, y2, 0xff6060b0, 0xff8070b0, fill);
	}

	public static void fill(DrawContext ctx, int x1, int y1, int x2, int y2, int colTop, int colBot, int colFill) {
		ctx.fill(x1, y1 + 1, x1 + 1, y2 - 1, colTop);
		ctx.fill(x1 + 1, y1, x2 - 1, y1 + 1, colTop);
		ctx.fill(x2 - 1, y1 + 1, x2, y2 - 1, colBot);
		ctx.fill(x1 + 1, y2 - 1, x2 - 1, y2, colBot);
		ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, colFill);
	}

	public static void horizontalGradient(DrawContext ctx, int x1, int y1, int x2, int y2, int color1, int color2) {
		// Fill with columns of interpolated colors, since DrawContext only exposes vertical gradients
		int width = x2 - x1;

		if (width <= 0) {
			return;
		}

		for (int i = 0; i < width; i++) {
			float progress = width == 1 ? 0 : i / (float) (width - 1);
			ctx.fill(x1 + i, y1, x1 + i + 1, y2, lerpColor(color1, color2, progress));
		}
	}

	public static void verticalGradient(DrawContext ctx, int x1, int y1, int x2, int y2, int color1, int color2) {
		ctx.fillGradient(x1, y1, x2, y2, color1, color2);
	}

	/**
	 * Fallback in case the mouse release event was never delivered to this window.
	 */
	protected void checkMouseRelease() {
		if (dragging && !isMouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)
				&& !isMouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)
				&& !isMouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
			dragging = false;
		}
	}

	private static boolean isMouseButtonDown(int button) {
		return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), button) == GLFW.GLFW_PRESS;
	}

	private static int lerpColor(int color1, int color2, float progress) {
		int a1 = color1 >> 24 & 255, r1 = color1 >> 16 & 255, g1 = color1 >> 8 & 255, b1 = color1 & 255;
		int a2 = color2 >> 24 & 255, r2 = color2 >> 16 & 255, g2 = color2 >> 8 & 255, b2 = color2 & 255;

		int a = (int) (a1 + (a2 - a1) * progress);
		int r = (int) (r1 + (r2 - r1) * progress);
		int g = (int) (g1 + (g2 - g1) * progress);
		int b = (int) (b1 + (b2 - b1) * progress);

		return a << 24 | r << 16 | g << 8 | b;
	}
}
