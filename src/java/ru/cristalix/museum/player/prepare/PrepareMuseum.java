package ru.cristalix.museum.player.prepare;

import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareMuseum implements Prepare {

	@Override
	public void execute(User user, App app) {
		user.getMuseums().get("main").load(user);
	}

}
