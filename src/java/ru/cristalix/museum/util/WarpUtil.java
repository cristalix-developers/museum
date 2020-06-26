package ru.cristalix.museum.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import ru.cristalix.museum.App;
import ru.cristalix.museum.gallery.Warp;
import ru.cristalix.museum.player.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author func 25.06.2020
 * @project museum
 */
@UtilityClass
public class WarpUtil {

	private static final Map<String, Warp> warps = new HashMap<>();

	public static Warp get(String address) {
		return warps.getOrDefault(address, null);
	}

	public class WarpBuilder {

		private final Warp warp;

		public WarpBuilder(String address) {
			if (warps.containsKey(address)) {
				warp = warps.get(address);
				return;
			}

			final Location[] start = new Location[1];
			final Location[] finish = new Location[1];

			App.getApp().getMap().getLabels().stream()
					.filter(label -> "warp".equals(label.getName()) && label.getTag().split(" ")[0].equals(address))
					.forEach(label -> {
						String[] ss = label.getTag().split(" ");
						if ("start".equals(ss[1]))
							start[0] = label.getBlock().getLocation();
						else if ("finish".equals(ss[1]))
							finish[0] = label.getBlock().getLocation();
					});
			warp = new Warp(address, start[0], finish[0]);
		}

		public WarpBuilder onForward(Consumer<User> onForward) {
			warp.setOnForward(onForward);
			return this;
		}

		public WarpBuilder onBack(Consumer<User> onBack) {
			warp.setOnBack(onBack);
			return this;
		}

		public WarpBuilder addAfter(Consumer<User> after) {
			warp.setAfter(after);
			return this;
		}

		public WarpBuilder addBefore(Consumer<User> before) {
			warp.setBefore(before);
			return this;
		}

		public WarpBuilder condition(Predicate<User> condition) {
			warp.setCondition(condition);
			return this;
		}

		public Warp build() {
			warps.put(warp.getAddress(), warp);
			return warp;
		}
	}
}
