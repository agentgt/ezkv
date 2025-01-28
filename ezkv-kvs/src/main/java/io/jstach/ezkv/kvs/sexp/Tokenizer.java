package io.jstach.ezkv.kvs.sexp;

import java.util.ArrayList;
import java.util.List;

class Tokenizer {
	enum TokenType {
		LEFT_PAREN, // (
		RIGHT_PAREN, // )
		SYMBOL, // Function name or variable
		STRING, // Quoted string
		BOOLEAN, // true, false
		NIL, // nil
		EOF
	}

	record Token(TokenType type, String value, int pos) {
	}

	public List<Token> tokenize(
			String input) {
		List<Token> tokens = new ArrayList<>();
		int length = input.length();
		int i = 0;

		while (i < length) {
			char c = input.charAt(i);

			if (Character.isWhitespace(c)) {
				// Skip whitespace
				i++;
			} else if (c == '(') {
				tokens.add(new Token(TokenType.LEFT_PAREN, "(", i));
				i++;
			} else if (c == ')') {
				tokens.add(new Token(TokenType.RIGHT_PAREN, ")", i));
				i++;
			} else if (c == '"') {
				// Start of a quoted string
				int start = ++i;
				StringBuilder sb = new StringBuilder();
				while (i < length && input.charAt(i) != '"') {
					char current = input.charAt(i);
					if (current == '\\') {
						// Handle escape sequence
						if (i + 1 < length) {
							char next = input.charAt(i + 1);
							sb.append(switch (next) {
							case 'n' -> '\n';
							case 't' -> '\t';
							case 'r' -> '\r';
							case '"' -> '"';
							case '\\' -> '\\';
							default -> throw new IllegalArgumentException("Invalid escape sequence: \\" + next);
							});
							i++;
						} else {
							throw new IllegalArgumentException("Unterminated escape sequence in string");
						}
					} else {
						sb.append(current);
					}
					i++;
				}
				if (i >= length || input.charAt(i) != '"') {
					throw new IllegalArgumentException("Unterminated string literal starting at position " + start);
				}
				i++; // Skip closing quote
				tokens.add(new Token(TokenType.STRING, sb.toString(), start));
			} else if (Character.isLetter(c) || c == '_') {
				// Start of a symbol or boolean/nil literal
				int start = i;
				while (i < length && (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
					i++;
				}
				String word = input.substring(start, i);
				switch (word) {
				case "true", "false" -> tokens.add(new Token(TokenType.BOOLEAN, word, start));
				case "nil" -> tokens.add(new Token(TokenType.NIL, "nil", start));
				default -> tokens.add(new Token(TokenType.SYMBOL, word, start));
				}
			} else {
				throw new IllegalArgumentException("Unexpected character: " + c);
			}
		}

		var eof = new Token(TokenType.EOF, "", -1);
		tokens.add(eof);
		return tokens;
	}
}
