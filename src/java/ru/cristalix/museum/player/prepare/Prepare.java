package ru.cristalix.museum.player.prepare;

import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;

@FunctionalInterface
public interface Prepare {

	void execute(User user, App app);

}
