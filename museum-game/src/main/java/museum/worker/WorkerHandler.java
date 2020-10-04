package museum.worker;

import museum.App;
import museum.player.User;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.*;
import static org.bukkit.util.NumberConversions.square;

/**
 * @author func 17.07.2020
 * @project museum
 */
public class WorkerHandler {

	private static final List<NpcWorker> workers = new ArrayList<>();
	private final String test =
			"http://textures.minecraft.net/texture/be1467a71faa590368b2e16d93a87cf390a2b0b70be309c9c1a39561261b2c27";

	public WorkerHandler(App app) {
		// Формат таблички: .p simplenpc <Имя жителя> </команда>
		workers.addAll(app.getMap().getLabels("simplenpc")
				.stream()
				.map(label -> {
					String[] ss = label.getTag().split("\\s+");
					return new NpcWorker(label, test, ss[0], user -> {
						if (ss[1].startsWith("/"))
							user.performCommand(label.getTag().substring(ss[0].length() + 2));
					});
				}).collect(Collectors.toList()));
		// Поворот голов фантомных игроков
		Bukkit.getScheduler().runTaskTimerAsynchronously(app, () -> {
			for (User user : app.getUsers()) {
				if (user.getPlayer() == null)
					continue;
				Location playerLoc = user.getLocation();
				for (NpcWorker worker : workers) {
					PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook pckg = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook();
					Location loc = worker.getLocation();
					float yawDeg = (float) toDegrees(atan2(loc.getX() - playerLoc.getX(), playerLoc.getZ() - loc.getZ()));
					byte yaw = (byte) ((int) (yawDeg * 256.0F / 360.0F));
					int id = worker.getMeta("id");
					pckg.a = id;
					pckg.e = yaw;
					float pitchDeg = (float) toDegrees(atan2(loc.getY() - playerLoc.getY(), sqrt(square(loc.getX() - playerLoc.getX()) + square(loc.getZ() - playerLoc.getZ()))));
					pckg.f = (byte) ((int) (pitchDeg * 256.0F / 360.0F));
					pckg.g = true;
					pckg.h = false;
					user.sendPacket(pckg);
					PacketPlayOutEntityHeadRotation rot = new PacketPlayOutEntityHeadRotation();
					rot.a = id;
					rot.b = yaw;
					user.sendPacket(rot);
				}
			}
		}, 1, 1);
	}

	public static void acceptClick(User user, int id) {
		for (NpcWorker worker : workers) {
			if (worker.getMeta("id").equals(id)) {
				worker.getOnInteract().accept(user);
				return;
			}
		}
	}

	public static void load(User user) {
		for (NpcWorker worker : workers) {
			worker.hide(user);
			worker.show(user);
		}
	}
}
