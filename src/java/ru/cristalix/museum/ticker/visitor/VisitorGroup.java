package ru.cristalix.museum.ticker.visitor;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.museum.Coin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author func 14.07.2020
 * @project museum
 */
public class VisitorGroup {

	private final List<Visitor> visitors = new ArrayList<>();
	private final App app;

	public VisitorGroup(App app, Location start, int amount) {
		this.app = app;
		for (float i = 0; i < amount; i++)
			visitors.add(new Visitor(start.getWorld()
					.spawnEntity(start, EntityType.VILLAGER), new V3(i / amount, 0, i % amount)));
	}

	public void move(Location meetingLocation) {
		visitors.forEach(visitor -> {
			val location = meetingLocation.clone().add(visitor.getDelta().getX(), 0, visitor.getDelta().getZ());

			visitor.getEntity().getNavigation().a(location.getX(), location.getY(), location.getZ(), .7);

			for (Player player : Bukkit.getOnlinePlayers()) {
				val user = app.getUser(player);
				if (user.getCurrentMuseum() == null || user.getCoins().size() > 50)
					continue;
				Coin coin = new Coin(visitor.getEntity().getBukkitEntity().getLocation());
				coin.create(user.getConnection());
				user.getCoins().add(coin);
			}
		});
	}
}
