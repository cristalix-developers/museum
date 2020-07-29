package ru.cristalix.core.hub;

import com.google.common.cache.CacheLoader;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FunctionalCacheLoader<K, V> extends CacheLoader<K, V> {

	private final ThrowingFunction<K, V> underlyingFunction;

	@Override
	public V load(K key) throws Exception {
		return underlyingFunction.apply(key);
	}

	public interface ThrowingFunction<K, V> {
		V apply(K key) throws Exception;
	}

}
