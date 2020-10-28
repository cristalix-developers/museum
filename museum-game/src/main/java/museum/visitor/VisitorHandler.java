package museum.visitor;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.val;
import museum.App;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class VisitorHandler {
	@Getter
	private static final Map<UUID, VisitorGroup> visitorUuids = Maps.newHashMap();

	public static void init(App app, Supplier<Integer> amountSupplier) {
		val labels = app.getMap().getLabels("node");
		val nodes = labels.stream()
				.filter(label -> label.getTag() != null)
				.collect(Collectors.toList());

		for (int i = 0; i < amountSupplier.get(); i++)
			new VisitorGroup(labels).spawn();

		if (labels.isEmpty())
			throw new RuntimeException("Not visitors nodes found.");

		Bukkit.getScheduler().runTaskTimer(app, () -> {
			val groups = new HashSet<>(visitorUuids.values());
			int amount = amountSupplier.get();
			int dif = groups.size() - amount;
			if (dif < 0) {
				new VisitorGroup(labels).spawn();
			} else if (dif > 0) {
				groups.stream().findFirst().ifPresent(groupToRemove -> {
					List<UUID> toDelete = new ArrayList<>(2);
					for (Map.Entry<UUID, VisitorGroup> entry : visitorUuids.entrySet())
						if (entry.getValue().equals(groupToRemove))
							toDelete.add(entry.getKey());
					toDelete.forEach(visitorUuids::remove);
					groupToRemove.remove();
				});
			}
		}, 20, 20);
	}
}
