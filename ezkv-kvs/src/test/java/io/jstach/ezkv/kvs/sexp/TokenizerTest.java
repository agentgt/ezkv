package io.jstach.ezkv.kvs.sexp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.jstach.ezkv.kvs.sexp.Tokenizer.Token;

class TokenizerTest {

	@Test
	void testTokenize() {
		String exp = """
				(and (eq k "port") (eq v "80"))
				""";
		Tokenizer tn = new Tokenizer();
		var tokens = tn.tokenize(exp);
		String actual = toStringTokens(tokens);
		String expected = """
				Token[type=LEFT_PAREN, value=(]
				Token[type=SYMBOL, value=and]
				Token[type=LEFT_PAREN, value=(]
				Token[type=SYMBOL, value=eq]
				Token[type=SYMBOL, value=k]
				Token[type=STRING, value=port]
				Token[type=RIGHT_PAREN, value=)]
				Token[type=LEFT_PAREN, value=(]
				Token[type=SYMBOL, value=eq]
				Token[type=SYMBOL, value=v]
				Token[type=STRING, value=80]
				Token[type=RIGHT_PAREN, value=)]
				Token[type=RIGHT_PAREN, value=)]
				Token[type=EOF, value=]
								""";
		assertEquals(expected, actual);
	}
	
	static String toStringTokens(List<Token> tokens) {
		return tokens.stream().map(t -> t.toString()).collect(Collectors.joining("\n"));
	}
}
