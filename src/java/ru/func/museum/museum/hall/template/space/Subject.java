package ru.func.museum.museum.hall.template.space;

import ru.func.museum.data.subject.SubjectInfo;
import ru.func.museum.player.User;

public interface Subject {

	SubjectInfo generateInfo();

    void show(User owner);

    void hide(User owner);

    double getIncome();

}
