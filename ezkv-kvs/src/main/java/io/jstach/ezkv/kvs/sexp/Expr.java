package io.jstach.ezkv.kvs.sexp;

import java.util.List;

sealed interface Expr permits Literal, Symbol, FunctionCall { }

//record Literal(Object value) implements Expr { } // Boolean, String, or nil

sealed interface Literal extends Expr {}
record StringLiteral(String value) implements Literal {}
record BoolLiteral(boolean value) implements Literal {}
enum NIL implements Literal {
	NIL;
}
record Symbol(String name) implements Expr { }  // Variable or function name
record FunctionCall(String function, List<Expr> arguments) implements Expr { }

