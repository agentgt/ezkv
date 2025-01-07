package io.jstach.ezkv.kvs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.jstach.ezkv.kvs.KeyValuesServiceProvider.KeyValuesLoaderFinder.LoaderContext;

class DefaultKeyValuesLoaderFinderTest {

	@Test
	void testManifest() throws Exception {
		var system = KeyValuesSystem.defaults();
		Variables variables = Variables.empty();
		KeyValuesResourceParser resourceParser = DefaultKeyValuesResourceParser.DEFAULT;
		LoaderContext ctx = new DefaultLoaderContext(system.environment(), 
				system.mediaFinder(), variables, resourceParser);
		String properties = """
				Manifest-Version=1.0
				Specification-Title=ezkv-kvs
				Specification-Vendor=ezkv
				Implementation-Title=ezkv-kvs
				Implementation-Vendor=ezkv
								""";
		var expected = KeyValuesMedia.ofProperties().parser().parse(properties).toMap();
		{
		KeyValuesResource resource = KeyValuesResource.builder("manifest:/").build();
		var map = system.loaderFinder().findLoader(ctx, resource).orElseThrow().load().toMap();
		assertSubset(expected, map);
		}
		{
		KeyValuesResource resource = KeyValuesResource.builder("manifest:///java.xml").build();
		var map = system.loaderFinder().findLoader(ctx, resource).orElseThrow().load().toMap();
		assertSubset(expected, map);
		}
		
	}
	
	void assertSubset(Map<String, String> expected, Map<String,String> actual) {
		Map<String, String> m = new LinkedHashMap<>();
		for (var k : expected.keySet()) {
			var v = actual.get(k);
			if (v == null) {
				continue;
			}
			m.put(k, v);
		}
		assertEquals(expected, m);
	}

}
