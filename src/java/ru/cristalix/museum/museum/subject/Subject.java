package ru.cristalix.museum.museum.subject;

import ru.cristalix.museum.Storable;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.player.User;

public interface Subject extends Storable<SubjectInfo> {

	void show(User user);

	default void update(User user) {}

	void hide(User user);

	default double getIncome() {
		return 0;
	}

}
