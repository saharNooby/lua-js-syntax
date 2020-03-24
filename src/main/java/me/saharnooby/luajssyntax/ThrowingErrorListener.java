package me.saharnooby.luajssyntax;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * @author saharNooby
 * @since 14:25 23.08.2019
 */
final class ThrowingErrorListener extends BaseErrorListener {

	static final BaseErrorListener INSTANCE = new ThrowingErrorListener();

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		throw new InvalidSyntaxException(line, charPositionInLine, msg, e);
	}

}
