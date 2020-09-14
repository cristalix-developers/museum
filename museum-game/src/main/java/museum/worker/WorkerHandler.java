package museum.worker;

import lombok.Getter;
import lombok.val;
import museum.App;
import museum.player.User;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author func 17.07.2020
 * @project museum
 */
public class WorkerHandler {

	@Getter
	private final List<Villager> workers;
	private static final PotionEffect SLOWNESS =
			new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10000, false, false);

	public WorkerHandler(App app) {
		// Формат таблички: .p simplenpc <Имя жителя> </команда>
		workers = app.getMap().getLabels("simplenpc").stream()
				.map(label -> {
					String[] ss = label.getTag().split("\\s+", 2);
					val villager = (Villager) app.getWorld().spawnEntity(label, EntityType.VILLAGER);
					villager.setCustomName(ss[0]);
					villager.setAI(false);
					villager.setCustomNameVisible(true);
					villager.addPotionEffect(SLOWNESS);
					String command = ss[1];
					if (command.startsWith("/")) command = command.substring(1);
					villager.setMetadata("command", new FixedMetadataValue(app, command));
					((CraftVillager) villager).getHandle().persistent = true;
					return villager;
				}).collect(Collectors.toList());
	}

	public void acceptClick(User user, Villager villager) {
		for (Villager worker : workers) {
			if (worker.equals(villager)) {
				user.performCommand(villager.getMetadata("command").get(0).asString());
				return;
			}
		}
	}

}
