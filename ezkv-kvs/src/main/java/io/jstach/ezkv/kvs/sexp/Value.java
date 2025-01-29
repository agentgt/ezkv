package io.jstach.ezkv.kvs.sexp;

sealed interface Value { }

//For boolean values
record BoolValue(boolean value) implements Value { }

//For string values
record StringValue(String value) implements Value { }

//For key-value tuples
record KeyValueValue(String key, String value) implements Value { }

//For nil (null-like behavior)
record NilValue() implements Value { }

non-sealed interface LookupValue extends Value {
	String get(String k);
}