package io.jstach.ezkv.kvs;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.regex.Pattern;

import io.jstach.ezkv.kvs.KeyValuesServiceProvider.KeyValuesFilter;

enum DefaultKeyValuesFilter implements KeyValuesFilter {

	GREP(KeyValuesResource.FILTER_GREP) {

		@Override
		protected KeyValues doFilter(FilterContext context, KeyValues keyValues, String expression, Target target) {
			String grep = expression;
			// TODO error handling.
			Objects.requireNonNull(keyValues);
			Pattern pattern = Pattern.compile(grep);
			return keyValues.filter(kv -> {
				String v = switch(target) {
				case KEY -> kv.key();
				case VALUE -> kv.value();
				};
				return pattern.matcher(v).find();
			});
		}
	},
	SED(KeyValuesResource.FILTER_SED) {

		@Override
		protected KeyValues doFilter(FilterContext context, KeyValues keyValues, String expression, Target target) {
			String sed = expression;
			Objects.requireNonNull(sed);
			var command = DefaultSedParser.parse(sed);
			return switch(target) {
			case KEY ->  keyValues.flatMap(kv -> {
				String key = command.execute(kv.key());
				if (key == null) {
					return KeyValues.empty();
				}
				if (kv.key().equals(key)) {
					return KeyValues.of(kv);
				}
				return KeyValues.of(kv.withKey(key));
			});
			case VALUE ->  keyValues.flatMap(kv -> {
				String value = command.execute(kv.value());
				if (value == null) {
					return KeyValues.empty();
				}
				if (kv.value().equals(value)) {
					return KeyValues.of(kv);
				}
				return KeyValues.of(kv.withExpanded(value));
			});
			};

		}

	},
	JOIN(KeyValuesResource.FILTER_JOIN) {
		@Override
		protected KeyValues doFilter(FilterContext context, KeyValues keyValues, String expression, Target target) {
			Objects.requireNonNull(keyValues);
			SequencedMap<String, KeyValue> m = new LinkedHashMap<>();
			for (var kv : keyValues) {
				var found = m.get(kv.key());
				if (found != null) {
					m.put(kv.key(), kv.withExpanded(found.expanded() + expression + kv.expanded()));
				}
				else {
					m.put(kv.key(), kv);
				}
			}
			return KeyValues.copyOf(m.values().stream().toList());
		}
	},

	;

	private final String filter;

	private DefaultKeyValuesFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public Optional<KeyValues> filter(FilterContext context, KeyValues keyValues, Filter filter) {
		String filterName = filter.filter();
		Target target;
		String resolvedFilterName;
//		String targetString = context.parameters().getValue("filter_target");
//		if (targetString != null) {
//			target = switch(targetString) {
//			case "key" -> Target.KEY;
//			case "value" ->Target.VALUE;
//			default -> 
//				throw new IllegalArgumentException("filter_target is invalid and should be 'key' or 'value'. got '" + targetString + "'");
//			};
//			resolvedFilterName = filterName;
//		}
		if (filterName.endsWith("_key")) {
			target = Target.KEY;
			resolvedFilterName = filterName.substring(0, filterName.length() - "_key".length());
		}
		else if (filterName.endsWith("_value")) {
			target = Target.VALUE;
			resolvedFilterName = filterName.substring(0, filterName.length() - "_value".length());
		}
		else {
			target = Target.KEY;
			resolvedFilterName = filterName;
		}
		
		if (this.filter.equalsIgnoreCase(resolvedFilterName)) {
			return Optional.of(doFilter(context, keyValues, filter.expression(), target));
		}
		return Optional.empty();
	}

	protected abstract KeyValues doFilter(FilterContext context, KeyValues keyValues, String expression, Target target);
	
	enum Target {
		KEY,
		VALUE;
	}

}
