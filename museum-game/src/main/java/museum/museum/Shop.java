package museum.museum;

import museum.App;
import museum.player.State;
import museum.player.User;
import museum.util.LocationUtil;
import museum.worker.WorkerHandler;
import org.bukkit.Location;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;

public class Shop implements State {

	private final Location spawnLocation;

	public Shop(App app) {
		this.spawnLocation = LocationUtil.resetLabelRotation(app.getMap().requireLabel("shop-spawn"), 0);
	}

	@Override
	public void setupScoreboard(User user, SimpleBoardObjective obj) {
		obj.setDisplayName("Магазин");
	}

	@Override
	public void enterState(User user) {
		user.teleport(this.spawnLocation);
		WorkerHandler.load(user);
	}

	@Override
	public void leaveState(User user) {

	}

}
