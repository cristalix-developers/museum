package ru.cristalix.museum.worker;

import clepto.bukkit.gui.Guis;
import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.util.warp.WarpUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author func 17.07.2020
 * @project museum
 */
public class WorkerHandler {

	private final List<Villager> workers;
	private static final PotionEffect SLOWNESS =
			new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10000, false, false);

	public WorkerHandler(App app) {
		// Формат таблички: .p worker <Название> <mover/shower> <meta>
		workers = app.getMap().getLabels("worker").stream()
				.map(label -> {
					String[] ss = label.getTag().split("\\s+");
					val villager = (Villager) app.getWorld().spawnEntity(label, EntityType.VILLAGER);
					villager.setCustomName(ss[0]);
					villager.setCustomNameVisible(true);
					villager.addPotionEffect(SLOWNESS);
					villager.setMetadata("type", new FixedMetadataValue(app, ss[1].toUpperCase()));
					villager.setMetadata("meta", new FixedMetadataValue(app, ss[2]));
					((CraftVillager) villager).getHandle().persistent = true;
					return villager;
				}).collect(Collectors.toList());
	}

	public void acceptClick(User user, Villager villager) {
		for (Villager worker : workers) {
			if (worker.equals(villager)) {
				WorkerType.valueOf(villager.getMetadata("type").get(0).asString())
						.onClick(user, villager.getMetadata("meta").get(0).asString());
				return;
			}
		}
	}

	@AllArgsConstructor
	enum WorkerType {
		MOVER() {
			@Override
			void onClick(User user, String meta) {
				WarpUtil.get(meta).warp(user);
			}
		},
		SHOWER() {
			@Override
			void onClick(User user, String meta) {
				Guis.registry.get(meta).open(user.getPlayer(), null);
			}
		},
		;

		abstract void onClick(User user, String meta);
	}
}
