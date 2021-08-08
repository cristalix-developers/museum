package museum.international;

import clepto.bukkit.item.Items;
import lombok.val;
import museum.App;
import museum.fragment.Fragment;
import museum.fragment.Gem;
import museum.player.State;
import museum.player.User;
import museum.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;

/**
 * @author func 14.02.2021
 * @project museum
 */
public class Market implements State {
	private final Location spawnLocation;

	private final ItemStack BACK_ITEM = Items.render("back").asBukkitMirror();

	public Market(App app) {
		this.spawnLocation = LocationUtil.resetLabelRotation(app.getMap().requireLabel("market-spawn"), 0);
	}

	@Override
	public void enterState(User user) {
		user.teleport(this.spawnLocation);
		val inventory = user.getInventory();
		inventory.clear();
		inventory.setItem(8, BACK_ITEM);

		user.getPlayer().setAllowFlight(false);
		user.getPlayer().setFlying(false);

		for (Fragment fragment : user.getRelics())
			if (fragment instanceof Gem)
				user.getInventory().addItem(fragment.getItem());

		user.sendMessage(
				"⟼  §6§lРынок",
				"",
				"    Обменивайтесь камнями с другими",
				"  игроками, берите §bкамень в руку §fи",
				"  нажмите §bправой кнопкой мыши§f,",
				"  зажимая §bс покупателем SHIFT."
		);
	}

	@Override
	public void leaveState(User user) {
	}

	@Override
	public boolean playerVisible() {
		return true;
	}

	@Override
	public boolean nightVision() {
		return false;
	}
}
