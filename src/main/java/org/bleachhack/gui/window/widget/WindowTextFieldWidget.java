package org.bleachhack.gui.window.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.text.Text;


public class WindowTextFieldWidget extends WindowWidget {

	public TextFieldWidget textField;

	public WindowTextFieldWidget(int x, int y, int width, int height, String text) {
		super(x, y, x + width, y + height);
		this.textField = new TextFieldWidget(mc.textRenderer, x, y, width, height, Text.empty());
		this.textField.setText(text);
		this.textField.setMaxLength(32767);
	}

	protected WindowTextFieldWidget(int x, int y, int width, int height) {
		super(x, y, x + width, y + height);
	}

	@Override
	public void render(DrawContext ctx, int windowX, int windowY, int mouseX, int mouseY) {
		textField.setX(windowX + x1);
		textField.setY(windowY + y1);
		textField.render(ctx, mouseX, mouseY, MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false));

		super.render(ctx, windowX, windowY, mouseX, mouseY);
	}

	@Override
	public void mouseClicked(int windowX, int windowY, int mouseX, int mouseY, int button) {
		super.mouseClicked(windowX, windowY, mouseX, mouseY, button);

		textField.mouseClicked(new Click(mouseX, mouseY, new MouseInput(button, 0)), false);
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void charTyped(char chr, int modifiers) {
		super.charTyped(chr, modifiers);

		textField.charTyped(new CharInput(chr, modifiers));
	}

	@Override
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		super.keyPressed(keyCode, scanCode, modifiers);

		textField.keyPressed(new KeyInput(keyCode, scanCode, modifiers));
	}
}
