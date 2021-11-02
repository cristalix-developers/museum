package museum.international;

import lombok.val;
import museum.App;
import museum.player.User;
import museum.util.LocationUtil;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;
import org.bukkit.Location;

public class Shop implements International {

	private final Location spawnLocation;

	public Shop(App app) {
		this.spawnLocation = LocationUtil.resetLabelRotation(app.getMap().requireLabel("shop-spawn"), 0);
	}

	@Override
	public void enterState(User user) {
		user.teleport(this.spawnLocation);
		val inventory = user.getInventory();
		inventory.clear();
		inventory.setItem(8, BACK_ITEM);
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
		return true;
	}

	@Override
	public void acceptBlockBreak(User user, PacketPlayInBlockDig packet) {
	}
}
