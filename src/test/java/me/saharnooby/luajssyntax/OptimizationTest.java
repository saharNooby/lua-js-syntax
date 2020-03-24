package me.saharnooby.luajssyntax;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author saharNooby
 * @since 20:57 21.08.2019
 */
class OptimizationTest {

	@Test
	void testForwardLoop() {
		String code = "for (let i = 0; i <= 10; i++) {\n" +
				"    consume(i)\n" +
				"}";

		String converted = LuaJSToLua.convert(code);

		Assertions.assertFalse(converted.contains("while"));
	}

	@Test
	void testReversedLoop() {
		String code = "for (let i = 10; i >= 0; i--) {\n" +
				"    consume(i)\n" +
				"}";

		String converted = LuaJSToLua.convert(code);

		Assertions.assertFalse(converted.contains("while"));
	}

	@Test
	void testForwardLoopCustomStep() {
		String code = "for (let i = 0; i <= 10; i += 2) {\n" +
				"    consume(i)\n" +
				"}";

		String converted = LuaJSToLua.convert(code);

		Assertions.assertFalse(converted.contains("while"));
	}

	@Test
	void testReversedLoopCustomStep() {
		String code = "for (let i = 10; i >= 0; i -= 2) {\n" +
				"    consume(i)\n" +
				"}";

		String converted = LuaJSToLua.convert(code);

		Assertions.assertFalse(converted.contains("while"));
	}

}
