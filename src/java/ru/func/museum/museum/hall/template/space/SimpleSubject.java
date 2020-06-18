package ru.func.museum.museum.hall.template.space;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.func.museum.element.Element;
import ru.func.museum.player.User;

import java.util.List;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SimpleSubject implements Subject {

	@Override
	public void show(User owner) {

	}

	@Override
	public void hide(User owner) {

	}

	@Override
	public double getIncome() {
		return 0;
	}

}
