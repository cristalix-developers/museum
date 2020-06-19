package ru.cristalix.museum.museum.subject;

import ru.cristalix.museum.Storable;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.player.User;

public interface Subject extends Storable<SubjectInfo> {

    void show(User owner);

    void hide(User owner);

    default double getIncome() {
    	return 0;
	}

}
