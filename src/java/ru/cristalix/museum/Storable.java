package ru.cristalix.museum;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@FunctionalInterface
public interface Storable<T> {

	static <T> List<T> store(Collection<? extends Storable<? extends T>> storables) {
		return storables.stream().map(Storable::generateInfo).collect(Collectors.toList());
	}

	T generateInfo();

}
