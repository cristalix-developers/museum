package museum.museum;

import museum.App;
import museum.player.State;
import museum.player.User;
import org.bukkit.Location;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;

public class Shop implements State {

	private final App app;
	private final Location spawnLocation;

	public Shop(App app) {
		this.app = app;
		this.spawnLocation = this.app.getMap().requireLabel("shop-spawn");
	}

	@Override
	public void setupScoreboard(User user, SimpleBoardObjective obj) {
		obj.setDisplayName("Магазин");
	}

	@Override
	public void enterState(User user) {
		user.teleport(this.spawnLocation);
	}

	@Override
	public void leaveState(User user) {

	}

}
