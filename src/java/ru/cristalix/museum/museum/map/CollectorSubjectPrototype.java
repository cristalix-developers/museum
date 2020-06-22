package ru.cristalix.museum.museum.map;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CollectorSubjectPrototype extends SubjectPrototype {

	private final double speed;
	private final double radius;

}
