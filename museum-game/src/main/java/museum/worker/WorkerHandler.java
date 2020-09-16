package museum.worker;

import com.mojang.authlib.GameProfile;
import museum.App;
import museum.player.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author func 17.07.2020
 * @project museum
 */
public class WorkerHandler {

	private static final List<NpcWorker> workers = new ArrayList<>();
	private final String test =
			"http://textures.minecraft.net/texture/8694879ed454829f43b5117f8770b3436feaa92865a45809ccb3cdecae24e28";

	public WorkerHandler(App app) {
		// Формат таблички: .p simplenpc <Имя жителя> </команда>
		workers.addAll(app.getMap().getLabels("simplenpc")
				.stream()
				.map(label -> {
					String[] ss = label.getTag().split("\\s+", 2);
					return new NpcWorker(label, test, ss[0], user -> {
						if (ss[1].startsWith("/"))
							user.performCommand(label.getTag().substring(1));
					});
				}).collect(Collectors.toList()));
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
		for (NpcWorker worker : workers)
			if (worker.getLocation().distanceSquared(user.getLocation()) < 100_000)
				worker.show(user);
	}
}
