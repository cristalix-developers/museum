package museum.player.prepare;

import museum.App;
import museum.player.User;

@FunctionalInterface
public interface Prepare {

	void execute(User user, App app);

}
