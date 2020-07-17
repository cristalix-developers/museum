package ru.cristalix.museum.ticker.detail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Skull;
import ru.cristalix.museum.App;
import ru.cristalix.museum.ticker.Ticked;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author func 17.07.2020
 * @project museum
 */
public class HeadRewardHandler implements Ticked {

	private static final long REWARD_RELOAD = 30 * 20L;
	private final Random random = new Random();
	private final List<HeadReward> rewards;
	private final String[] strings = {"307264a1-2c69-11e8-b5ea-1cb72caa35fd"};

	public HeadRewardHandler(App app) {
		rewards = app.getMap().getLabels("head").stream()
				.map(label -> new HeadReward(label.getBlock().getLocation(), label.getTagDouble(), false))
				.collect(Collectors.toList());
	}

	public HeadReward getHeadRewardByLocation(Location location) {
		for (HeadReward headReward : rewards)
			if (headReward.active && headReward.location.equals(location))
				return headReward;
		return null;
	}

	@Override
	public void tick(int... args) {
		if (args[0] % REWARD_RELOAD != 0)
			return;
		rewards.get(random.nextInt(rewards.size())).generate(UUID.fromString(strings[random.nextInt(strings.length)]));
	}

	@AllArgsConstructor
	public static class HeadReward {
		private final Location location;
		@Getter
		private final double reward;
		private boolean active;

		public void generate(UUID owner) {
			location.getBlock().setType(Material.SKULL);

			Skull skull = (Skull) location.getBlock().getState();

			// todo: skin not working
			skull.setSkullType(SkullType.PLAYER);
			skull.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
			skull.update();
			active = true;
		}

		public void remove() {
			location.getBlock().setType(Material.AIR);
			active = false;
		}
	}
}
