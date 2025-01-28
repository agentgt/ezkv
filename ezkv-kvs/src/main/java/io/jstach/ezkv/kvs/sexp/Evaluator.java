package io.jstach.ezkv.kvs.sexp;

import java.util.List;
import java.util.Map;

class Evaluator {

	private final Map<String, Value> context;

	Evaluator(
			Map<String, Value> context) {
		this.context = context;
	}

	public Value evaluate(
			Expr expr) {
		if (expr instanceof Literal literal) {
			return evaluateLiteral(literal);
		} else if (expr instanceof Symbol symbol) {
			return evaluateSymbol(symbol);
		} else if (expr instanceof FunctionCall functionCall) {
			return evaluateFunctionCall(functionCall);
		}
		throw new IllegalArgumentException("Unknown expression type: " + expr);
	}

	private Value evaluateLiteral(
			Literal literal) {
		Value v = switch(literal) {
		case StringLiteral sl -> new StringValue(sl.value());
		case BoolLiteral bl -> new BoolValue(bl.value());
		case NIL n ->  new NilValue();
		};
		return v;
	}

	private Value evaluateSymbol(
			Symbol symbol) {
		String name = symbol.name();
		var v= context.get(name);
		if (v != null)  {
			return v;
		}
		throw new IllegalArgumentException("Unbound symbol: " + name);
	}

	private Value evaluateFunctionCall(
			FunctionCall functionCall) {
		String functionName = functionCall.function();
		List<Expr> arguments = functionCall.arguments();

		return switch (functionName) {
		case "eq" -> evaluateEq(arguments);
		case "and" -> evaluateAnd(arguments);
		case "or" -> evaluateOr(arguments);
		default -> throw new UnsupportedOperationException("Unknown function: " + functionName);
		};
	}

	// Built-in Functions
	private BoolValue evaluateEq(
			List<Expr> args) {
		if (args.size() != 2) {
			throw new IllegalArgumentException("eq expects exactly 2 arguments");
		}
		Value left = evaluate(args.get(0));
		Value right = evaluate(args.get(1));

		if (left instanceof StringValue leftStr && right instanceof StringValue rightStr) {
			return new BoolValue(leftStr.value().equals(rightStr.value()));
		} else if (left instanceof KeyValueValue leftKv && right instanceof KeyValueValue rightKv) {
			return new BoolValue(leftKv.equals(rightKv));
		}
		throw new IllegalArgumentException("eq expects arguments of the same type");
	}

	private BoolValue evaluateAnd(
			List<Expr> args) {
		for (Expr arg : args) {
			Value result = evaluate(arg);
			if (!(result instanceof BoolValue boolValue)) {
				throw new IllegalArgumentException("and expects boolean arguments");
			}
			if (!boolValue.value()) {
				return new BoolValue(false); // Short-circuit on false
			}
		}
		return new BoolValue(true);
	}

	private BoolValue evaluateOr(
			List<Expr> args) {
		for (Expr arg : args) {
			Value result = evaluate(arg);
			if (!(result instanceof BoolValue boolValue)) {
				throw new IllegalArgumentException("or expects boolean arguments");
			}
			if (boolValue.value()) {
				return new BoolValue(true); // Short-circuit on true
			}
		}
		return new BoolValue(false);
	}
}
