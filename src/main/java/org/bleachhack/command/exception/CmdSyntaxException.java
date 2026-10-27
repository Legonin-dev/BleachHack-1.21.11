package org.bleachhack.command.exception;

import net.minecraft.text.Text;

import java.io.Serial;

public class CmdSyntaxException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 7940377774005961331L;

	private final Text textMessage;

	public CmdSyntaxException() {
        this("Invalid Syntax!");
    }

    public CmdSyntaxException(String message) {
        this(Text.literal(message));
    }
    
    public CmdSyntaxException(Text message) {
        // RuntimeException can no longer hold a Text, so keep the component around separately.
        super(message.getString());
        this.textMessage = message;
    }

    /**
     * @return the original message component, so it can be logged with its formatting intact.
     */
    public Text getTextMessage() {
        return textMessage;
    }

}
