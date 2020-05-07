package me.saharnooby.luajssyntax;

import me.saharnooby.luajssyntax.exception.InvalidSyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author saharNooby
 * @since 14:17 23.08.2019
 */
class ErrorTest {

	@Test
	void testInvalidCharacters() {
		Assertions.assertThrows(InvalidSyntaxException.class, () -> LuaJSToLua.convert("test() \0"));
	}

	@Test
	void testSyntaxError() {
		Assertions.assertThrows(InvalidSyntaxException.class, () -> LuaJSToLua.convert("let x = 1 # 2"));
	}

}
