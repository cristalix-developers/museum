package ru.cristalix.museum;

import java.util.List;
import java.util.stream.Collectors;

public interface Storable<T> {

	T generateInfo();

	static <T> List<T> store(List<? extends Storable<? extends T>> list) {
		return list.stream().map(Storable::generateInfo).collect(Collectors.toList());
	}

}
