package ru.cristalix.museum.museum.map;

import clepto.cristalix.Box;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.subject.Subject;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.prototype.Prototype;

import java.util.List;

@Data
@SuperBuilder
public class SubjectPrototype implements Prototype {

	private final String address;
	private final SubjectType<?> type;
	private final double price;
	private final Box box;
	private final String title;
	private final int cristalixPrice;
	private final V3 relativeOrigin;
	private final List<V3> relativeManipulators;

	public Subject provide(SubjectInfo info, User owner) {
		return type.provide(this, info, owner);
	}

}
