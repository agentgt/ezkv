package io.jstach.kiwi.kvs;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.jspecify.annotations.Nullable;

import io.jstach.kiwi.kvs.KeyValuesServiceProvider.LoaderFinder;
import io.jstach.kiwi.kvs.KeyValuesServiceProvider.MediaFinder;

public interface KeyValuesSystem {

	public KeyValuesEnvironment environment();

	public LoaderFinder loaderFinder();

	public MediaFinder mediaFinder();

	default KeyValuesLoader.Builder loader() {
		return new KeyValuesLoader.Builder(DefaultKeyValuesResourceLoader.of(this, Variables.empty()));
	}

	public static KeyValuesSystem defaults() {
		return builder().build();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private @Nullable KeyValuesEnvironment environment;

		private List<LoaderFinder> loadFinders = new ArrayList<>(List.of(DefaultLoaderFinder.values()));

		private List<MediaFinder> mediaFinders = new ArrayList<>(List.of(DefaultKeyValuesMedia.values()));

		private @Nullable ServiceLoader<KeyValuesServiceProvider> serviceLoader;

		public Builder environment(KeyValuesEnvironment environment) {
			this.environment = environment;
			return this;
		}

		public Builder loadFinder(LoaderFinder loadFinder) {
			this.loadFinders.add(loadFinder);
			return this;
		}

		public Builder mediaFinder(MediaFinder mediaFinder) {
			this.mediaFinders.add(mediaFinder);
			return this;
		}

		public Builder serviceLoader(ServiceLoader<KeyValuesServiceProvider> serviceLoader) {
			this.serviceLoader = serviceLoader;
			return this;
		}

		public KeyValuesSystem build() {
			var environment = this.environment;
			if (environment == null) {
				environment = new DefaultKeyValuesEnvironment();
			}
			var loadFinders = new ArrayList<>(this.loadFinders);
			var mediaFinders = new ArrayList<>(this.mediaFinders);
			var serviceLoader = this.serviceLoader;

			if (serviceLoader != null) {
				serviceLoader.forEach(s -> {
					if (s instanceof LoaderFinder rl) {
						loadFinders.add(rl);
					}
					if (s instanceof MediaFinder m) {
						mediaFinders.add(m);
					}
				});
			}

			LoaderFinder loadFinder = (context, resource) -> {
				return loadFinders.stream().flatMap(rl -> rl.findLoader(context, resource).stream()).findFirst();
			};
			MediaFinder mediaFinder = new CompositeMediaFinder(mediaFinders);

			return new DefaultKeyValuesSystem(environment, loadFinder, mediaFinder);
		}

	}

}

record CompositeMediaFinder(List<MediaFinder> finders) implements MediaFinder {
	CompositeMediaFinder {
		finders = List.copyOf(finders);
	}

	@Override
	public Optional<KeyValuesMedia> findByExt(String ext) {
		return finders.stream().flatMap(mf -> mf.findByExt(ext).stream()).findFirst();
	}

	@Override
	public Optional<KeyValuesMedia> findByMediaType(String mediaType) {
		return finders.stream().flatMap(mf -> mf.findByMediaType(mediaType).stream()).findFirst();

	}

	@Override
	public Optional<KeyValuesMedia> findByUri(URI uri) {
		return finders.stream().flatMap(mf -> mf.findByUri(uri).stream()).findFirst();
	}

}

record DefaultKeyValuesSystem(KeyValuesEnvironment environment, LoaderFinder loaderFinder,
		MediaFinder mediaFinder) implements KeyValuesSystem {

}
