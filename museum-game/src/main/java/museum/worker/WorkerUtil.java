package museum.worker;

import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author func 17.07.2020
 * @project museum
 */
@UtilityClass
public class WorkerUtil {

	private final static String defaultSkin = App.getApp().getConfig().getString("npc.default.skin");
	public static final Supplier<NpcWorker> STALL_WORKER_TEMPLATE = () -> new NpcWorker(
			new Location(App.getApp().getWorld(), 0, 0, 0),
			defaultSkin,
			"Работница лавки",
			User::getExperience
	);
	private static final List<NpcWorker> workers = new ArrayList<>();

	public void init(App app) {
		// Формат таблички: .p simplenpc <Имя жителя> </команда>
		workers.addAll(app.getMap().getLabels("simplenpc")
				.stream()
				.map(label -> {
					ConfigurationSection data = app.getConfig().getConfigurationSection("npc." + label.getTag().split("\\s+")[0]);
					if (data == null) {
						return STALL_WORKER_TEMPLATE.get();
					} else {
						val hint = (ArmorStand) label.world.spawnEntity(
								label.clone().add(.5, 2.1, .5),
								EntityType.ARMOR_STAND
						);
						hint.setGravity(false);
						hint.setCustomName(data.getString("hint"));
						hint.setMarker(true);
						hint.setVisible(false);
						hint.setCustomNameVisible(true);
						return new NpcWorker(
								label.clone().add(.5, 0, .5),
								data.getString("skin"),
								data.getString("title"),
								user -> user.performCommand(data.getString("command"))
						);
					}
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