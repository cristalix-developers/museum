package museum.player.prepare;

import com.destroystokyo.paper.Title;
import museum.App;
import museum.player.User;

/**
 * @author func 26.08.2020
 * @project museum
 */
public class PreparePlayerBrain implements Prepare {
	@Override
	public void execute(User user, App app) {
		if (user.getPlayer().hasPlayedBefore())
			return;
		user.getPlayer().sendTitle(new Title("1", "2"));
	}
}
