package io.jstach.ezkv.kvs.sexp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.jstach.ezkv.kvs.sexp.Tokenizer.Token;
import io.jstach.ezkv.kvs.sexp.Tokenizer.TokenType;

class Parser {
	private final Iterator<Token> tokens;
	Token current = new Token(TokenType.EOF, "", -1);
	Token previous = current;

	public Parser(
			Iterator<Token> tokens) {
		this.tokens = tokens;
	}
	
	public static Expr parse(String input) {
		Tokenizer t = new Tokenizer();
		var tokens = t.tokenize(input);
		Parser p = new Parser(tokens.iterator());
		return p.parse();
	}

	public Expr parse() {
		advance();
		return parseExpression();
	}
	
	private Expr parseExpression() {
		if (isAtEnd()) {
			throw new IllegalArgumentException("at end");
		}
		var token = advance();
		Expr expr = switch(token.type()) {
		case LEFT_PAREN -> { 
			yield parseFunctionCall();
		}
		case STRING ->  {
			yield new StringLiteral(token.value());
		}
		case BOOLEAN -> {
			yield new BoolLiteral(Boolean.parseBoolean(token.value()));
		}
		case NIL -> {
			yield NIL.NIL;
		}
		case SYMBOL -> {
			yield new Symbol(token.value());
		}
		case RIGHT_PAREN, EOF -> throw new IllegalArgumentException("Unexpected token: " + token);
		};
		
		return expr;
	}


	private FunctionCall parseFunctionCall() {
		// Parse a function call starting with "("
		var token = advance();
		String functionName = switch(token.type()) {
		case SYMBOL -> token.value();
		default -> throw new IllegalArgumentException("Expected function name after '('");
		};

		List<Expr> arguments = new ArrayList<>();
		
		while (current.type() != Tokenizer.TokenType.RIGHT_PAREN) {
			if (isAtEnd()) {
				throw new IllegalArgumentException("Unterminated function call");
			}
			arguments.add(parseExpression());
		}
		advance();
		return new FunctionCall(functionName, arguments);
	}
	
	private Token advance() {
		previous = Objects.requireNonNull(current);
		current = tokens.next();
		return Objects.requireNonNull(previous);
	}

	private boolean isAtEnd() {
		return current.type() == TokenType.EOF;
	}

}
