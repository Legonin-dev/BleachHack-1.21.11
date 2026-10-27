package org.bleachhack.gui.window.widget;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class WindowPassTextFieldWidget extends WindowTextFieldWidget {

	public WindowPassTextFieldWidget(int x, int y, int width, int height, String text) {
		super(x, y, width, height, text);
		this.textField.addFormatter((string, cursor) -> Text.literal("\u2022".repeat(string.length())).asOrderedText());
	}
}
