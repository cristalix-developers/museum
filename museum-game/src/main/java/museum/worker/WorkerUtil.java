package museum.worker;

import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author func 17.07.2020
 * @project museum
 */
@UtilityClass
public class WorkerUtil {

	// todo: remove that shit in future
	public static final NpcWorker STALL_WORKER_TEMPLATE = new NpcWorker(
			new Location(App.getApp().getWorld(), 0, 0, 0),
			"http://textures.minecraft.net/texture/be1467a71faa590368b2e16d93a87cf390a2b0b70be309c9c1a39561261b2c27",
			"Работница лавки",
			null
	);
	private static final List<NpcWorker> workers = new ArrayList<>();
	private final String test =
			"http://textures.minecraft.net/texture/be1467a71faa590368b2e16d93a87cf390a2b0b70be309c9c1a39561261b2c27";

	public void init(App app) {
		// Формат таблички: .p simplenpc <Имя жителя> </команда>
		workers.addAll(app.getMap().getLabels("simplenpc")
				.stream()
				.map(label -> {
					String[] ss = label.getTag().split("\\s+");
					return new NpcWorker(label, test, ss[0], user -> {
						if (ss.length < 2)
							return;
						if (ss[1].startsWith("/"))
							user.performCommand(label.getTag().substring(ss[0].length() + 2));
					});
				}).collect(Collectors.toList()));
		// Поворот голов фантомных игроков
		Bukkit.getScheduler().runTaskTimerAsynchronously(app, () -> {
			for (User user : app.getUsers()) {
				if (user.getPlayer() == null)
					continue;
				val playerLoc = V4.fromLocation(user.getLocation());

				for (NpcWorker worker : workers)
					worker.update(user, playerLoc);
			}
		}, 20, 6);
	}

	public void acceptClick(User user, int id) {
		for (NpcWorker worker : workers) {
			if (worker.getMeta("id").equals(id)) {
				worker.getOnInteract().accept(user);
				return;
			}
		}
	}

	public void reload(User user) {
		for (NpcWorker worker : workers) {
			worker.hide(user);
			worker.show(user);
		}
	}
}