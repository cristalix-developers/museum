package ru.cristalix.museum.museum.subject;

import ru.cristalix.museum.Storable;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.player.User;

public interface Subject extends Storable<SubjectInfo> {

	SubjectType<?> getType();

	void show(User user);

	// todo: useless method
	default void update(User user) {
	}

	void hide(User user, boolean visually);

	default double getIncome() {
		return 0;
	}

	default Allocation getAllocation() {
		return null;
	}

}
