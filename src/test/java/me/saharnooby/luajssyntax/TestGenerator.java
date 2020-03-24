package me.saharnooby.luajssyntax;

import java.util.*;

/**
 * Used to generate random test cases for validating operator priority.
 * @author saharNooby
 * @since 12:33 23.08.2019
 */
public final class TestGenerator {

	private static final String[] OPS = new String[]{
			"+",
			"-",
			"*",
			"/",
			"%",
			"<",
			">",
			"<=",
			">=",
			"==",
			"!=",
			"&&",
			"||",
			"**",
	};

	private static final String[] UNARY = new String[]{"!", "-", "~", "#"};

	private static final Set<String> COMPARISON = new HashSet<>(Arrays.asList("<", ">", "<=", ">=", "==", "!="));

	private static final Set<String> ARITHMETIC = new HashSet<>(Arrays.asList("+", "-", "*", "/", "%", "**"));

	private static final Map<String, String> OP_TO_LUA = new HashMap<>();

	static {
		OP_TO_LUA.put("!=", "~=");
		OP_TO_LUA.put("&&", "and");
		OP_TO_LUA.put("||", "or");
		OP_TO_LUA.put("**", "^");
		OP_TO_LUA.put("!", "not ");
		OP_TO_LUA.put("~", "bit32.bnot");
	}

	public static void main(String[] args) {
		generatePrioritiesWithUnary();
	}

	private static void generatePriorities() {
		Random r = new Random(123);

		StringBuilder builder = new StringBuilder();
		StringBuilder lua = new StringBuilder();

		for (String op1 : OPS) {
			for (String op2 : OPS) {
				if (COMPARISON.contains(op1) && COMPARISON.contains(op2)) {
					continue;
				}

				String num1 = num(r);
				String num2 = num(r);
				String num3 = num(r);

				builder.append("consume(")
						.append(num1)
						.append(" ")
						.append(op1)
						.append(" ")
						.append(num2)
						.append(" ")
						.append(op2)
						.append(" ")
						.append(num3)
						.append(")\n");

				lua.append("consume(")
						.append(num1)
						.append(" ")
						.append(OP_TO_LUA.getOrDefault(op1, op1))
						.append(" ")
						.append(num2)
						.append(" ")
						.append(OP_TO_LUA.getOrDefault(op2, op2))
						.append(" ")
						.append(num3)
						.append(")\n");
			}

			builder.append("\n");
			lua.append("\n");
		}

		System.out.println(builder);
		System.out.println("\n===\n\n");
		System.out.println(lua);
	}

	private static void generatePrioritiesWithUnary() {
		Random r = new Random(123);

		StringBuilder builder = new StringBuilder();
		StringBuilder lua = new StringBuilder();

		for (String op1 : OPS) {
			for (String op2 : UNARY) {
				if (op2.equals("#")) {
					continue;
				}

				if (op2.equals("!") && (ARITHMETIC.contains(op1) || COMPARISON.contains(op1))) {
					continue;
				}

				String num1 = num(r);
				String num2 = num(r);
				String num3 = num(r);
				String num4 = num(r);

				builder.append("consume(")
						.append(op2)
						.append(" ")
						.append(num1)
						.append(" ")
						.append(op1)
						.append(" ")
						.append(num2)
						.append(")\n");

				builder.append("consume(")
						.append(num3)
						.append(" ")
						.append(op1)
						.append(" ")
						.append(op2)
						.append(" ")
						.append(num4)
						.append(")\n");

				// ---

				lua.append("consume(");
				lua.append(OP_TO_LUA.getOrDefault(op2, op2));
				if (op2.equals("~")) {
					lua.append("(");
				}
				lua.append(" ");
				lua.append(num1);
				if (op2.equals("~")) {
					lua.append(")");
				}
				lua.append(" ");
				lua.append(OP_TO_LUA.getOrDefault(op1, op1));
				lua.append(" ");
				lua.append(num2);
				lua.append(")\n");

				lua.append("consume(");
				lua.append(num3);
				lua.append(" ");
				lua.append(OP_TO_LUA.getOrDefault(op1, op1));
				lua.append(" ");
				lua.append(OP_TO_LUA.getOrDefault(op2, op2));
				if (op2.equals("~")) {
					lua.append("(");
				}
				lua.append(" ");
				lua.append(num4);
				if (op2.equals("~")) {
					lua.append(")");
				}
				lua.append(")\n");
			}

			builder.append("\n");
			lua.append("\n");
		}


		System.out.println(builder);
		System.out.println("\n===\n\n");
		System.out.println(lua);
	}

	private static String num(Random r) {
		return r.nextInt(100) + "." + r.nextInt(100);
	}

}
