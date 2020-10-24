package museum.player.prepare;

import lombok.val;
import museum.App;
import museum.player.User;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareJSAnime implements Prepare {

	public static final Prepare INSTANCE = new PrepareJSAnime();
	public static final String AVAILABLE_SCRIPTS = "museum amongus markuptext";

	private static final String[] separated = AVAILABLE_SCRIPTS.split("\\s+");

	@Override
	public void execute(User user, App app) {
		for (val script : separated)
			user.performCommand("u " + script);
	}
}