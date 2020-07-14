package ru.cristalix.museum.visitor;

import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.museum.Coin;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class Visitor {

	@Getter
	private final EntityInsentient entity;
	private final App app = App.getApp();

	public Visitor(org.bukkit.entity.Entity entity) {
		this.entity = (EntityInsentient) ((CraftEntity) entity).getHandle();
	}

	public void visit(Location meetingLocation) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			val user = app.getUser(player);
			if (user.getCurrentMuseum() == null || user.getCoins().size() > 50)
				continue;
			Coin coin = new Coin(meetingLocation);
			coin.create(user.getConnection());
			user.getCoins().add(coin);
		}

		entity.ticksLived = 0;
		entity.getNavigation().a(
				meetingLocation.getX(),
				meetingLocation.getY(),
				meetingLocation.getZ(),
				.6
		);
	}

}
