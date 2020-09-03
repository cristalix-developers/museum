package museum.worker;

import lombok.AllArgsConstructor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import museum.App;

/**
 * @author func 17.07.2020
 * @project museum
 */
@AllArgsConstructor
public class WorkerClickListener implements Listener {

	private final App app;
	private final WorkerHandler manager;

	@EventHandler
	public void onWorkerClick(PlayerInteractEntityEvent event) {
		if (event.getRightClicked().getType() == EntityType.VILLAGER)
			manager.acceptClick(app.getUser(event.getPlayer()), (Villager) event.getRightClicked());
	}
}
