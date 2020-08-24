package museum.ticker.visitor;

import clepto.cristalix.mapservice.Label;
import clepto.cristalix.mapservice.MapServiceException;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.cristalix.core.math.V3;
import museum.App;
import museum.museum.Coin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author func 14.07.2020
 * @project museum
 */
@Getter
public class VisitorGroup {

	private final List<Visitor> visitors = new ArrayList<>();
	private final List<Label> route;
	private final String name;
	private int counter = 0;
	private boolean prepared = false;

	public VisitorGroup(String name) {
		this.name = name;
		this.route = new ArrayList<>();
	}

	public void next() {
		if (!prepared)
			prepare();

		val app = App.getApp();

		counter = ++counter % (route.size() * 2);

		visitors.forEach(visitor -> {
			val location = route.get(counter / 2).clone().add(visitor.getDelta().getX(), 0, visitor.getDelta().getZ());

			visitor.getEntity().getNavigation().a(location.getX(), location.getY(), location.getZ(), .9);

			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getLocation().distanceSquared(location) > 10000)
					continue;
				val user = app.getUser(player);
				if (user.getCurrentMuseum() == null || user.getCoins().size() > 50)
					continue;
				Coin coin = new Coin(visitor.getEntity().getBukkitEntity().getLocation());
				coin.create(user.getConnection());
				user.getCoins().add(coin);
			}
		});
	}

	private void prepare() {
		if (route.isEmpty())
			throw new MapServiceException("Not found visitor route!");

		route.sort(Comparator.comparingInt(dot -> Integer.parseInt(dot.getTag().split("\\s+")[1])));

		val startDot = route.get(0);

		val visitorCount = VisitorHandler.VISITORS_IN_GROUP;
		for (float i = 0; i < visitorCount; i++)
			visitors.add(new Visitor(startDot.getWorld()
					.spawnEntity(startDot, EntityType.VILLAGER), new V3(i / visitorCount, 0, i % visitorCount)));
		prepared = true;
	}
}
