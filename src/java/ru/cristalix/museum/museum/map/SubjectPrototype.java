package ru.cristalix.museum.museum.map;

import clepto.cristalix.Box;
import lombok.Data;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.Prototype;

import java.util.List;

@Data
public class SubjectPrototype implements Prototype {

	private final String address;
	private final SubjectType type;
	private final double price;
	private final Box box;
	private final V3 relativeOrigin;
	private final List<V3> relativeManipulators;

}
