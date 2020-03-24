package me.saharnooby.luajssyntax;

import lombok.NonNull;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * This class provides a method to convert LuaJS source code to Lua code.
 * @author saharNooby
 * @since 19:06 21.08.2019
 */
public final class LuaJSToLua {

	/**
	 * Converts LuaJS source code to Lua, preserving line numbers.
	 * @param source LuaJS source code, must be not null.
	 * @return Lua source code, will be not null.
	 * @throws InvalidSyntaxException When provided source is invalid.
	 */
	public static String convert(@NonNull String source) {
		StringBuilder sb = new StringBuilder();
		convert(source, sb);
		return sb.toString();
	}

	/**
	 * Converts LuaJS source code to Lua, preserving line numbers. Resulting code will be appended to the specified Appendable.
	 * @param source LuaJS source code, must be not null.
	 * @param out Destination for writing the result code, must be not null.
	 * @throws InvalidSyntaxException When provided source is invalid.
	 * @throws java.io.UncheckedIOException When out throws an IOException.
	 */
	public static void convert(@NonNull String source, @NonNull Appendable out) {
		convert(CharStreams.fromString(source), HashUtil.md5(source), out);
	}

	private static void convert(@NonNull CharStream in, @NonNull String sourceHash, @NonNull Appendable out) {
		LuaJSSyntaxLexer lexer = new LuaJSSyntaxLexer(in);

		lexer.removeErrorListeners();
		lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		LuaJSSyntaxParser parser = new LuaJSSyntaxParser(tokens);

		parser.removeErrorListeners();
		parser.addErrorListener(ThrowingErrorListener.INSTANCE);

		LuaJSSyntaxParser.ProgramContext program = parser.program();

		new LuaJSToLuaConverter(new Printer(out), sourceHash).print(program);
	}

}
