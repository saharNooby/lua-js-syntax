package me.saharnooby.luajssyntax;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.opentest4j.AssertionFailedError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author saharNooby
 * @since 20:25 21.08.2019
 */
class ConverterTest {

	@Test
	void testLocals() {
		test();
	}

	@Test
	void testGlobals() {
		test();
	}

	@Test
	void testCompound() {
		test();
	}

	@Test
	void testBreak() {
		test();
	}

	@Test
	void testLabel() {
		test();
	}

	@Test
	void testReturn() {
		test();
	}

	@Test
	void testIf() {
		test();
	}

	@Test
	void testDoWhile() {
		test();
	}

	@Test
	void testFor() {
		test();
	}

	@Test
	void testForOptimized() {
		test();
	}

	@Test
	void testForIn() {
		test();
	}

	@Test
	void testForOf() {
		test();
	}

	@Test
	void testIncrement() {
		test();
	}

	@Test
	void testParenthesis() {
		test();
	}

	@Test
	void testLiterals() {
		test();
	}

	@Test
	void testNumbers() {
		test();
	}

	@Test
	void testStrings() {
		test();
	}

	@Test
	void testTables() {
		test();
	}

	@Test
	void testLists() {
		test();
	}

	@Test
	void testFunctionLiteral() {
		test();
	}

	@Test
	void testArrowFunctionLiteral() {
		test();
	}

	@Test
	void testUnary() {
		test();
	}

	@Test
	void testPower() {
		test();
	}

	@Test
	void testMath() {
		test();
	}

	@Test
	void testConcat() {
		test();
	}

	@Test
	void testComparison() {
		test();
	}

	@Test
	void testLogic() {
		test();
	}

	@Test
	void testBitwise() {
		test();
	}

	@Test
	void testPriorities() {
		test();
	}

	@Test
	void testPrioritiesUnary() {
		test();
	}

	@Test
	void testPrioritiesBitwise() {
		test();
	}

	@Test
	void testChainCalls() {
		test();
	}

	@Test
	void testTernary() {
		test();
	}

	@Test
	void testOOP() {
		test();
	}

	@Test
	void testContinue() {
		test();
	}

	@Test
	void testTryCatch() {
		test();
	}

	@Test
	void testThrow() {
		test();
	}

	// The behavior of this method is caller dependent.
	private void test() {
		String testName = new Exception().getStackTrace()[1].getMethodName().substring(4);

		StringBuilder jsCode = new StringBuilder();
		StringBuilder luaCode = new StringBuilder();

		StringBuilder current = jsCode;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(ConverterTest.class.getResourceAsStream(testName + ".txt"), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals("===")) {
					current = luaCode;
					continue;
				}

				current.append(line).append("\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String convertedCode = LuaJSToLua.convert(jsCode.toString());

		List<LuaValue> real;

		try {
			real = runScript(convertedCode);
		} catch (RuntimeException e) {
			System.err.println("Converted code");
			System.err.println(convertedCode);

			throw e;
		}

		List<LuaValue> expected;

		try {
			expected = runScript(luaCode.toString());
		} catch (RuntimeException e) {
			System.err.println("Lua check code");
			System.err.println(luaCode);

			throw e;
		}

		try {
			for (int i = 0; i < Math.min(real.size(), expected.size()); i++) {
				assertLuaValuesEquals(expected.get(i), real.get(i), "#" + i);
			}

			Assertions.assertEquals(expected.size(), real.size(), "call count mismatch");
		} catch (AssertionFailedError e) {
			System.err.println("Converted code");
			System.err.println(convertedCode);

			throw e;
		}
	}

	private List<LuaValue> runScript(@NonNull String script) {
		Globals globals = new Globals();

		globals.load(new JseBaseLib());
		globals.load(new PackageLib());
		globals.load(new Bit32Lib());
		globals.load(new TableLib());
		globals.load(new StringLib());
		globals.load(new JseMathLib());

		LuaC.install(globals);

		globals.set("tostring", new LibFunction() {

			public LuaValue call(LuaValue arg) {
				LuaValue meta = arg.metatag(TOSTRING);

				if (!meta.isnil()) {
					return meta.call(arg);
				}

				if (arg.istable()) {
					return valueOf("table");
				}

				LuaValue luaString = arg.tostring();

				if (!luaString.isnil()) {
					return luaString;
				}

				return valueOf(arg.tojstring());
			}

		});

		List<LuaValue> list = new ArrayList<>();

		globals.set("consume", new VarArgFunction() {

			@Override
			public Varargs invoke(Varargs varargs) {
				for (int i = 1; i <= varargs.narg(); i++) {
					list.add(varargs.arg(i));
				}

				return NONE;
			}
		});

		globals.load(script, "main", globals).call();

		return list;
	}

	private static void assertTableEquals(LuaTable x, LuaTable y, String message) {
		Assertions.assertEquals(x.keyCount(), y.keyCount());

		for (int i = 0; i < x.keyCount(); i++) {
			LuaValue kx = x.keys()[i];
			LuaValue ky = y.keys()[i];

			assertLuaValuesEquals(kx, ky, message);
			assertLuaValuesEquals(x.get(kx), y.get(ky), message);
		}
	}

	private static void assertLuaValuesEquals(LuaValue kx, LuaValue ky, String message) {
		if ((kx instanceof LuaTable) != (ky instanceof LuaTable)) {
			throw new AssertionError(kx + ", " + ky);
		}

		if (kx instanceof LuaTable) {
			assertTableEquals((LuaTable) kx, (LuaTable) ky, message);
		} else {
			Assertions.assertEquals(kx, ky, message);
		}
	}

}
