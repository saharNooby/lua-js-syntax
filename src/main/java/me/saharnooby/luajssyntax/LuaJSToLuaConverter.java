package me.saharnooby.luajssyntax;

import lombok.NonNull;
import me.saharnooby.luajssyntax.exception.InvalidSyntaxException;
import me.saharnooby.luajssyntax.util.Printer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author saharNooby
 * @since 16:28 20.08.2019
 */
@SuppressWarnings("unused")
final class LuaJSToLuaConverter {

	private final Printer printer;
	/**
	 * MD5 hash of the source code.
	 */
	private final String sourceHash;

	/**
	 * Current loop nesting level, 0 == not inside loop currently.
	 * This value is id of current label used to emulate continue statement.
	 */
	private int loopLevel = 0;
	/**
	 * Set of used ids of continue labels.
	 * Unused labels will not be emitted.
	 */
	private final Set<Integer> usedLabels = new HashSet<>();

	/**
	 * Index of the variable used to store pcall status.
	 */
	private int nextStatusVarIndex;

	public LuaJSToLuaConverter(@NonNull Printer printer, @NonNull String sourceHash) {
		this.printer = printer;
		this.sourceHash = sourceHash;
	}

	private void print(String s) {
		this.printer.print(s);
	}

	private void line(Token token) {
		this.printer.setExpectedLine(token.getLine());
	}

	public void print(LuaJSSyntaxParser.ProgramContext ctx) {
		print(ctx.statement());
	}

	// region Statements

	private void print(LuaJSSyntaxParser.SemicolonContext ctx) {

	}

	private void print(LuaJSSyntaxParser.BreakContext ctx) {
		print("break");
	}

	private void print(LuaJSSyntaxParser.ContinueContext ctx) {
		if (this.loopLevel == 0) {
			throw new InvalidSyntaxException(ctx.start.getLine(), ctx.start.getCharPositionInLine(), "continue outside of loop", null);
		}

		print("goto ");
		print(getContinueLabel());

		this.usedLabels.add(this.loopLevel);
	}

	private void print(LuaJSSyntaxParser.BlockStatementContext ctx) {
		print(ctx, true);
	}

	private void print(LuaJSSyntaxParser.BlockStatementContext ctx, boolean printDoEnd) {
		if (printDoEnd) {
			print("do ");
		}

		print(ctx.block().statement());

		if (printDoEnd) {
			print(" end");
		}
	}

	private void printWithoutDoEnd(LuaJSSyntaxParser.StatementContext ctx) {
		if (ctx instanceof LuaJSSyntaxParser.BlockStatementContext) {
			print((LuaJSSyntaxParser.BlockStatementContext) ctx, false);
		} else {
			print(ctx);
		}
	}

	private void print(LuaJSSyntaxParser.LocalVariableDeclarationContext ctx) {
		print("local ");
		print(ctx.namelist());

		if (ctx.explist() != null) {
			print(" = ");
			print(ctx.explist());
		}
	}

	private void print(LuaJSSyntaxParser.GlobalVariableDeclarationContext ctx) {
		print(ctx.varlist());
		print(" = ");
		print(ctx.explist());
	}

	private void print(LuaJSSyntaxParser.AssginmentOperatorContext ctx) {
		print(ctx.var());
		print(" = ");
		print(ctx.var(), ctx.assignmentOperator().start, ctx.exp(), true);
	}

	private void print(LuaJSSyntaxParser.FunctionCallContext ctx) {
		print(ctx.var());
		print(ctx.nameAndArgs());
	}

	private void print(LuaJSSyntaxParser.LabelDeclarationContext ctx) {
		print("::");
		print(ctx.NAME());
		print("::");
	}

	private void print(LuaJSSyntaxParser.GotoContext ctx) {
		print("goto ");
		print(ctx.NAME());
	}

	private void print(LuaJSSyntaxParser.ReturnContext ctx) {
		LuaJSSyntaxParser.ExplistContext explist = ctx.explist();

		if (explist != null) {
			print("return ");
			print(explist);
		} else {
			print("return");
		}
	}

	private void print(LuaJSSyntaxParser.IfContext ctx) {
		print("if (");
		print(ctx.exp());
		print(") then ");
		printWithoutDoEnd(ctx.statement(0));

		if (ctx.statement().size() > 1) {
			print(" else ");
			printWithoutDoEnd(ctx.statement(1));
		}

		print(" end");
	}

	private void print(LuaJSSyntaxParser.WhileContext ctx) {
		print("while (");
		print(ctx.exp());
		print(") do ");
		onLoopStart();
		printWithoutDoEnd(ctx.statement());
		onLoopEnd();
		print(" end");
	}

	private void print(LuaJSSyntaxParser.DoWhileContext ctx) {
		print("repeat ");
		onLoopStart();
		printWithoutDoEnd(ctx.statement());
		onLoopEnd();
		print(" until (not (");
		print(ctx.exp());
		print("))");
	}

	private void print(LuaJSSyntaxParser.ForContext ctx) {
		if (printOptimizedLoop(ctx)) {
			return;
		}

		print("do ");

		if (ctx.init != null) {
			print(ctx.init);
			print("; ");
		}

		print("while ");

		if (ctx.exp() != null) {
			print("(");
			print(ctx.exp());
			print(")");
		} else {
			print("true");
		}

		print(" do ");
		onLoopStart();
		printWithoutDoEnd(ctx.body);
		onLoopEnd();

		if (ctx.after != null) {
			print(" ");
			print(ctx.after);
			print("; ");
		}

		print(" end");
		print(" end");
	}

	private boolean printOptimizedLoop(LuaJSSyntaxParser.ForContext ctx) {
		//for (let x = start_number; x (<=|>=|<|>) limit_number; x++|x+=step_number|x--|x-=step_number) statement

		if (!(ctx.init instanceof LuaJSSyntaxParser.LocalVariableDeclarationContext)) {
			return false;
		}

		LuaJSSyntaxParser.LocalVariableDeclarationContext var = (LuaJSSyntaxParser.LocalVariableDeclarationContext) ctx.init;

		if (var.namelist().NAME().size() != 1) {
			return false;
		}

		if (var.explist() == null || var.explist().exp().size() != 1) {
			return false;
		}

		if (!(var.explist().exp(0) instanceof LuaJSSyntaxParser.NumberLiteralContext)) {
			return false;
		}

		String name = var.namelist().NAME(0).getText();
		String start = var.explist().exp(0).getText();

		if (!(ctx.exp() instanceof LuaJSSyntaxParser.ComparisonOperatorContext)) {
			return false;
		}

		LuaJSSyntaxParser.ComparisonOperatorContext comp = (LuaJSSyntaxParser.ComparisonOperatorContext) ctx.exp();

		if (!(comp.exp(0) instanceof LuaJSSyntaxParser.VarExpressionContext)) {
			return false;
		}

		if (!(comp.exp(1) instanceof LuaJSSyntaxParser.NumberLiteralContext)) {
			return false;
		}

		if (!comp.exp(0).getText().equals(name)) {
			return false;
		}

		String op = comp.op.getText();

		if (!op.contains(">") && !op.contains("<")) {
			return false;
		}

		if (!op.contains("=")) {
			return false;
		}

		String stop = comp.exp(1).getText();

		String step;

		if (ctx.after instanceof LuaJSSyntaxParser.IncrementContext) {
			step = "1";
		} else if (ctx.after instanceof LuaJSSyntaxParser.DecrementContext) {
			step = "-1";
		} else {
			LuaJSSyntaxParser.AssginmentOperatorContext after = (LuaJSSyntaxParser.AssginmentOperatorContext) ctx.after;

			String assignOp = after.assignmentOperator().getText();

			if (!assignOp.equals("+=") && !assignOp.equals("-=")) {
				return false;
			}

			if (!after.var().getText().equals(name)) {
				return false;
			}

			if (!(after.exp() instanceof LuaJSSyntaxParser.NumberLiteralContext)) {
				return false;
			}

			String stepNumber = after.exp().getText();

			step = assignOp.equals("-=") ? "-" + stepNumber : stepNumber;
		}

		if (step == null) {
			return false;
		}

		print("for ");
		print(name);
		print(" = ");
		print(start);
		print(", ");
		print(stop);
		print(", ");
		print(step);
		print(" do ");
		onLoopStart();
		printWithoutDoEnd(ctx.body);
		onLoopEnd();
		print(" end");

		return true;
	}

	private void print(LuaJSSyntaxParser.ForInContext ctx) {
		print("for ");
		print(ctx.namelist());
		print(" in ");
		print(ctx.exp());
		print(" do ");
		onLoopStart();
		printWithoutDoEnd(ctx.statement());
		onLoopEnd();
		print(" end");
	}

	private void print(LuaJSSyntaxParser.ForOfContext ctx) {
		print("for ");

		if (ctx.NAME().size() > 1) {
			print(ctx.NAME(1));
		} else {
			print("_");
		}

		print(", ");
		print(ctx.NAME(0));

		print(" in ipairs(");
		print(ctx.exp());
		print(") do ");
		onLoopStart();
		printWithoutDoEnd(ctx.statement());
		onLoopEnd();
		print(" end");
	}

	private void print(LuaJSSyntaxParser.FunctionDeclarationContext ctx) {
		print("function ");

		for (int i = 0; i < ctx.funcname().NAME().size(); i++) {
			if (i > 0) {
				print(":");
			}

			print(ctx.funcname().NAME().get(i));
		}

		print("(");

		if (ctx.namelist() != null) {
			print(ctx.namelist());
		}

		print(") ");
		print(ctx.block().statement());
		print(" end");
	}

	private void print(LuaJSSyntaxParser.TryCatchContext ctx) {
		line(ctx.start);
		print("do local ");

		String suffix = this.sourceHash.substring(0, 8) + "_" + this.nextStatusVarIndex++;

		String resName = "res_" + suffix;
		String eName = "e_" + suffix;

		print(resName);
		print(", ");
		print(eName);
		print(" = pcall(function() ");

		print(ctx.block(0).statement());

		print(" end); if not ");
		print(resName);
		print(" then local ");
		print(ctx.NAME().getText());
		print(" = ");
		print(eName);
		print("; ");

		print(ctx.block(1).statement());

		print(" end end");
	}

	private void print(LuaJSSyntaxParser.ThrowContext ctx) {
		line(ctx.start);
		print("error(");
		print(ctx.exp());
		print(")");
	}

	private void print(LuaJSSyntaxParser.DecrementContext ctx) {
		print(ctx.var());
		print(" = ");
		print(ctx.var());
		print(" - 1");
	}

	private void print(LuaJSSyntaxParser.IncrementContext ctx) {
		print(ctx.var());
		print(" = ");
		print(ctx.var());
		print(" + 1");
	}

	// endregion

	// region Expressions

	private void print(LuaJSSyntaxParser.ParenthesisExpressionContext ctx) {
		print("(");
		print(ctx.exp());
		print(")");
	}

	private void print(LuaJSSyntaxParser.LiteralContext ctx) {
		print(ctx.getText());
	}

	private void print(LuaJSSyntaxParser.StringLiteralContext ctx) {
		StringBuilder builder = new StringBuilder();

		String text = ctx.getText();

		builder.append(text.charAt(0));

		for (int i = 1; i < text.length() - 1; i++) {
			if (text.charAt(i) == '\\' && text.charAt(i + 1) == 'u') {
				++i;
				int a = Character.digit(text.charAt(++i), 16);
				int b = Character.digit(text.charAt(++i), 16);
				int c = Character.digit(text.charAt(++i), 16);
				int d = Character.digit(text.charAt(++i), 16);
				builder.append((char) ((a << 12) + (b << 8) + (c << 4) + d));
			} else {
				builder.append(text.charAt(i));
			}
		}

		builder.append(text.charAt(text.length() - 1));

		print(builder.toString());
	}

	private void print(LuaJSSyntaxParser.NumberLiteralContext ctx) {
		print(ctx.getText());
	}

	private void print(LuaJSSyntaxParser.FunctionLiteralContext ctx) {
		print("function (");

		if (ctx.namelist() != null) {
			print(ctx.namelist());
		}

		print(") ");
		print(ctx.block().statement());
		print(" end");
	}

	private void print(LuaJSSyntaxParser.ArrowFunctionLiteralContext ctx) {
		print("function (");

		if (ctx.namelist() != null) {
			print(ctx.namelist());
		} else if (ctx.NAME() != null) {
			print(ctx.NAME());
		}

		print(") ");

		if (ctx.exp() != null) {
			print("return ");
			print(ctx.exp());
		} else {
			print(ctx.block().statement());
		}

		print(" end");
	}

	private void print(LuaJSSyntaxParser.VarExpressionContext ctx) {
		print(ctx.var());
	}

	private void print(LuaJSSyntaxParser.TernaryOperatorContext ctx) {
		print("((");
		print(ctx.exp(0));
		print(") and (");
		print(ctx.exp(1));
		print(") or (");
		print(ctx.exp(2));
		print("))");
	}

	private void print(LuaJSSyntaxParser.FunctionCallExpressionContext ctx) {
		print(ctx.var());
		print(ctx.nameAndArgs());
	}

	private void print(LuaJSSyntaxParser.TableExpressionContext ctx) {
		print("{");

		if (ctx.table().entries() != null) {
			printCommaSeparated(ctx.table().entries().entry(), this::print);
		}

		print("}");
	}

	private void print(LuaJSSyntaxParser.ListExpressionContext ctx) {
		print("{");

		if (ctx.list().elements() != null) {
			printCommaSeparated(ctx.list().elements().exp(), this::print);
		}

		print("}");
	}

	private void print(LuaJSSyntaxParser.PowerOperatorContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.UnaryOperatorContext ctx) {
		String op = ctx.op.getText();

		if (op.equals("~")) {
			print("bit32.bnot(");
			print(ctx.exp());
			print(")");
			return;
		}

		if (op.equals("!")) {
			print("(not ");
			print(ctx.exp());
			print(")");
			return;
		}

		print(op);
		print(ctx.exp());
	}

	private void print(LuaJSSyntaxParser.MulDivModOperatorContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.AddSubOperatorContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.ConcatOperatorContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.ComparisonOperatorContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.AndOperatorContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.OrOperatorContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.BitwiseShiftContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.BitwiseAndContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.BitwiseXorContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	private void print(LuaJSSyntaxParser.BitwiseOrContext ctx) {
		print(ctx.exp(0), ctx.op, ctx.exp(1));
	}

	// endregion

	private void print(List<LuaJSSyntaxParser.StatementContext> statements) {
		for (int i = 0; i < statements.size(); i++) {
			print(statements.get(i));
			print(";");

			if (i < statements.size() - 1) {
				print(" ");
			}
		}
	}

	private void print(LuaJSSyntaxParser.NamelistContext ctx) {
		printCommaSeparated(ctx.NAME(), this::print);
	}

	private void print(LuaJSSyntaxParser.VarlistContext ctx) {
		printCommaSeparated(ctx.var(), this::print);
	}

	private void print(LuaJSSyntaxParser.VarContext ctx) {
		int offset;

		if (ctx.NAME() != null) {
			print(ctx.NAME());

			offset = 0;
		} else {
			line(ctx.exp().start);
			print("(");
			print(ctx.exp());
			print(")");
			print(ctx.varSuffix(0));

			offset = 1;
		}

		for (int i = offset; i < ctx.varSuffix().size(); i++) {
			print(ctx.varSuffix(i));
		}
	}

	private void print(LuaJSSyntaxParser.VarSuffixContext ctx) {
		if (ctx.nameAndArgs() != null) {
			for (LuaJSSyntaxParser.NameAndArgsContext arg : ctx.nameAndArgs()) {
				print(arg);
			}
		}

		if (ctx.exp() != null) {
			line(ctx.exp().start);
			print("[");
			print(ctx.exp());
			print("]");
		} else {
			line(ctx.NAME().getSymbol());
			print(".");
			print(ctx.NAME());
		}
	}

	private void print(LuaJSSyntaxParser.NameAndArgsContext ctx) {
		line(ctx.start);

		if (ctx.NAME() != null) {
			print(":");
			print(ctx.NAME());
		}

		print(ctx.args());
	}

	private void print(LuaJSSyntaxParser.ArgsContext ctx) {
		line(ctx.start);
		print("(");

		if (ctx.explist() != null) {
			print(ctx.explist());
		}

		print(")");
	}

	private void print(LuaJSSyntaxParser.ExplistContext explist) {
		printCommaSeparated(explist.exp(), this::print);
	}

	private void print(LuaJSSyntaxParser.EntryContext ctx) {
		if (ctx.NAME() != null) {
			print(ctx.NAME());
		} else {
			print("[");
			print(ctx.key_expr().exp());
			print("]");
		}

		print("=");
		print(ctx.exp());
	}

	private void print(ParserRuleContext left, Token opToken, LuaJSSyntaxParser.ExpContext right) {
		print(left, opToken, right, false);
	}

	private void print(ParserRuleContext left, Token opToken, LuaJSSyntaxParser.ExpContext right, boolean compound) {
		String op = opToken.getText();

		if (compound) {
			op = op.substring(0, op.length() - 1);
		}

		String call = null;

		switch (op) {
			case "&":
				call = "bit32.band";
				break;
			case "|":
				call = "bit32.bor";
				break;
			case "^":
				call = "bit32.bxor";
				break;
			case "<<":
				call = "bit32.lshift";
				break;
			case ">>":
				call = "bit32.rshift";
				break;
			case "**":
				op = "^";
				break;
			case "&&":
				op = "and";
				break;
			case "||":
				op = "or";
				break;
			case "!=":
				op = "~=";
				break;
		}

		boolean wrapLeft = op.equals("..") && !isConcatOrString(left);
		boolean wrapRight = op.equals("..") && !isConcatOrString(right);

		if (call != null) {
			line(opToken);
			print(call);
			print("(");
		}

		if (wrapLeft) {
			print("tostring(");
		}

		if (left instanceof LuaJSSyntaxParser.VarContext) {
			print((LuaJSSyntaxParser.VarContext) left);
		} else if (left instanceof LuaJSSyntaxParser.ExpContext) {
			print((LuaJSSyntaxParser.ExpContext) left);
		}

		if (wrapLeft) {
			print(")");
		}

		if (call != null) {
			print(", ");
			print(right);
			print(")");
		} else {
			print(" ");
			line(opToken);
			print(op);
			print(" ");

			if (wrapRight) {
				print("tostring(");
			}

			print(right);

			if (wrapRight) {
				print(")");
			}
		}

	}

	private void print(Token token) {
		line(token);
		print(token.getText());
	}

	private void print(TerminalNode node) {
		print(node.getSymbol());
	}

	private void onLoopStart() {
		this.loopLevel++;
	}

	private void onLoopEnd() {
		if (this.usedLabels.remove(this.loopLevel)) {
			print("::");
			print(getContinueLabel());
			print("::;");
		}

		this.loopLevel--;
	}

	private String getContinueLabel() {
		return "continue_" + this.sourceHash.substring(0, 8) + "_" + this.loopLevel;
	}

	// Utils

	private <T extends ParseTree> void printCommaSeparated(@NonNull List<T> list, @NonNull Consumer<T> printer) {
		for (int i = 0; i < list.size(); i++) {
			printer.accept(list.get(i));

			if (i < list.size() - 1) {
				print(", ");
			}
		}
	}

	private static boolean isConcatOrString(ParserRuleContext exp) {
		return exp instanceof LuaJSSyntaxParser.ConcatOperatorContext || exp instanceof LuaJSSyntaxParser.StringLiteralContext;
	}

	// Dynamic dispatching

	private void print(LuaJSSyntaxParser.StatementContext ctx) {
		line(ctx.start);
		dispatch(ctx, LuaJSSyntaxParser.StatementContext.class);
	}

	private void print(LuaJSSyntaxParser.ExpContext ctx) {
		line(ctx.start);
		dispatch(ctx, LuaJSSyntaxParser.ExpContext.class);
	}

	private <T extends ParserRuleContext> void dispatch(T argument, Class<T> baseClass) {
		try {
			//noinspection JavaReflectionMemberAccess
			Method method = getClass().getDeclaredMethod("print", argument.getClass());

			if (method.getParameterTypes()[0] == baseClass) {
				throw new IllegalArgumentException("Invalid input");
			}

			method.invoke(this, argument);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException)e.getTargetException();
			} else {
				throw new RuntimeException(e);
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

}
