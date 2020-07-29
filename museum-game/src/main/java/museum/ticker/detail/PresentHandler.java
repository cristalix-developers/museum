package museum.ticker.detail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Skull;
import museum.App;
import museum.player.User;
import museum.ticker.Ticked;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author func 17.07.2020
 * @project museum
 */
public class PresentHandler implements Ticked {

	private static final long REWARD_RELOAD = 30 * 20L;
	private final Random random = new Random();
	private final List<Present> rewards;
	private final String[] strings = {"307264a1-2c69-11e8-b5ea-1cb72caa35fd"};

	public PresentHandler(App app) {
		// Формат таблички: .p head <common/epic/legendary>
		rewards = app.getMap().getLabels("head").stream()
				.map(label -> new Present(
						label.getBlock().getLocation(),
						PresentType.valueOf(label.getTag().toUpperCase()),
						false
				)).collect(Collectors.toList());
	}

	public Present getPresentByLocation(Location location) {
		for (Present present : rewards)
			if (present.active && present.location.equals(location))
				return present;
		return null;
	}

	@Override
	public void tick(int... args) {
		if (args[0] % REWARD_RELOAD != 0)
			return;
		rewards.get(random.nextInt(rewards.size())).generate(UUID.fromString(strings[random.nextInt(strings.length)]));
	}

	@Getter
	@AllArgsConstructor
	public enum PresentType {
		COMMON(50, ((user, location) -> {})),
		EPIC(100, ((user, location) -> {})),
		LEGENDARY(500, ((user, location) -> {})),
		;

		private double price;
		private Founded onFind;
	}

	@FunctionalInterface
	public interface Founded {
		void onFind(User user, Location location);
	}

	@AllArgsConstructor
	public static class Present {
		private final Location location;
		@Getter
		private PresentType type;
		private boolean active;

		public void generate(UUID owner) {
			location.getBlock().setType(Material.SKULL);
			Skull skull = (Skull) location.getBlock().getState();
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
