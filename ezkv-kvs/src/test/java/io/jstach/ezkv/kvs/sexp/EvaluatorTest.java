package io.jstach.ezkv.kvs.sexp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.jstach.ezkv.kvs.KeyValue;

class EvaluatorTest {

	@Test
	void testEvaluate() {
		Map<String, Value> context = Map.of(
				"k",
				new StringValue("port"), // Current key
				"v",
				new StringValue("80"), // Current value
				"it",
				new KeyValueValue("port", "80"), // Current key-value pair
				"vars",
				new NilValue() // Empty variables map
		);
		String input = """
				(and (eq k "port") (eq v "80"))
				""";
		var exp = Parser.parse(input);
		Evaluator eval = new Evaluator(context);
		var value = eval.evaluate(exp);
		String actual = value.toString();
		String expected = """
				BoolValue[value=true]
				""".trim();
		assertEquals(expected, actual);
	}

}
