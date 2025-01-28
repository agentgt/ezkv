package io.jstach.ezkv.kvs.sexp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ParserTest {
	
	String input = """
			(and (eq k "port") (eq v "80"))
			""";

	@Test
	void testParse() {
		Tokenizer t = new Tokenizer();
		var tokens = t.tokenize(input);
		Parser parser = new Parser(tokens.iterator());
		var exp = parser.parse();
		String actual = reformatToString(exp.toString());
		String expected = """
				
				""";
		assertEquals(expected, actual);
		
	}
	
	static String reformatToString(String inputString) {
		StringBuilder result = new StringBuilder();
		int indentationLevel = 0;

		boolean skip = false;
		for (int i = 0; i < inputString.length(); i++) {
			char c = inputString.charAt(i);
			if (skip) {
				skip = false;
				if (c == ' ') {
					continue;
				}
			}
			if (c == '[') {
				indentationLevel++;
				result.append(c).append('\n').append("\t".repeat(indentationLevel));
			}
			else if (c == ']') {
				indentationLevel--;
				result.append('\n').append("\t".repeat(indentationLevel)).append(c);
			}
			else if (c == ',') {
				result.append(c).append('\n').append("\t".repeat(indentationLevel));
				skip = true;
			}
			else {
				result.append(c);
			}
		}

		return result.toString();
	}

}
