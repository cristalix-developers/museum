package ru.cristalix.museum.museum.map;

import clepto.cristalix.Box;
import lombok.Data;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.prototype.Prototype;

import java.util.List;

@Data
public class MuseumPrototype implements Prototype {

	private final String address;
	private final Box box;
	private final List<SubjectInfo> defaultSubjects;

}
