package ru.cristalix.museum.museum.subject;

import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.player.User;

public class MarkerSubject implements Subject {

	private final SubjectInfo info;

	public MarkerSubject(Museum museum, SubjectInfo subjectInfo, SubjectPrototype prototype) {
		this.info = subjectInfo;
//		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void show(User user) {

	}

	@Override
	public void hide(User user) {

	}

	@Override
	public SubjectInfo generateInfo() {
		return info;
	}

}
