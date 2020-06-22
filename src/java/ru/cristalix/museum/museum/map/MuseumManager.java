package ru.cristalix.museum.museum.map;

import clepto.cristalix.*;
import lombok.Getter;
import ru.cristalix.museum.App;
import ru.cristalix.museum.Manager;

import java.util.stream.Collectors;

@Getter
public class MuseumManager extends Manager<MuseumPrototype> {

	public MuseumManager() {
		super("museum");
	}

	@Override
	protected MuseumPrototype readBox(String address, Box box) {
		return new MuseumPrototype(address, this,
				box.requireLabel("spawn"),
				box.getLabels("default").stream()
						.map(label -> App.getApp().getSubjectManager().getPrototype(label.getTag()))
						.collect(Collectors.toList())
		);;
	}
}
