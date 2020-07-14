package ru.cristalix.museum.visitor;

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
			visitor.getEntity().ticksLived = 0;
			visitor.getEntity().getNavigation().a(
					meetingLocation.getX() + visitor.getDelta().getX(),
					meetingLocation.getY(),
					meetingLocation.getZ() + visitor.getDelta().getZ(),
					.9
			);

			for (Player player : Bukkit.getOnlinePlayers()) {
				val user = app.getUser(player);
				if (user.getCurrentMuseum() == null || user.getCoins().size() > 50)
					continue;
				Coin coin = new Coin(meetingLocation);
				coin.create(user.getConnection());
				user.getCoins().add(coin);
			}
		});
	}
}
