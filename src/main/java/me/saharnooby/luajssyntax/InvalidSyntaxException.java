package me.saharnooby.luajssyntax;

import lombok.Getter;
import lombok.NonNull;
import org.antlr.v4.runtime.RecognitionException;

/**
 * Thrown when LuaJS syntax is invalid.
 * It can have a {@link RecognitionException} as a cause, or null.
 * @author saharNooby
 * @since 14:22 23.08.2019
 */
@Getter
public final class InvalidSyntaxException extends RuntimeException {

	private static final long serialVersionUID = 3565251758944402880L;

	/**
	 * Line number with offending construction or character.
	 * Line numbers start from 1.
	 */
	private final int line;
	/**
	 * The position of the first char of the offending construction.
	 * Char positions start from 0.
	 */
	private final int charPosition;
	/**
	 * Some message from ANTLR, specifying what's wrong.
	 */
	private final String originalMessage;

	InvalidSyntaxException(int line, int charPosition, @NonNull String message, RecognitionException cause) {
		super("line " + line + ":" + charPosition + " " + message, cause);
		this.originalMessage = message;
		this.line = line;
		this.charPosition = charPosition;
	}

}
