package ru.cristalix.museum.museum.subject;

import lombok.experimental.Delegate;
import lombok.val;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.collector.CollectorNavigator;
import ru.cristalix.museum.museum.collector.CollectorType;
import ru.cristalix.museum.museum.subject.skeleton.Piece;
import ru.cristalix.museum.player.User;

import java.util.ArrayList;
import java.util.List;

public class CollectorSubject implements Subject {

	private final Museum museum;

	@Delegate
	private final SubjectInfo info;

	private final CollectorNavigator navigator;
	private final List<Piece> pieces = new ArrayList<>();

	public CollectorSubject(Museum museum, SubjectInfo info) {
		this.museum = museum;
		this.info = info;


		pieces.add(new Piece(, new Location(world, 0, 0, 0)));
	}

	@Override
	public SubjectInfo generateInfo() {
		return info;
	}

	@Override
	public void show(User user) {
		val location = getLocation(System.currentTimeMillis());
		for (Piece piece : pieces)
			piece.show(user.getPlayer(), location);
	}

	public void move(User user, long iteration) {
		val location = getLocation(iteration);

		user.getCoins().removeIf(coin -> coin.pickUp(user, location, getType().getRadius()));

		for (Piece piece : pieces)
			piece.update(user.getPlayer(), location);
	}

	@Override
	public void hide(User user) {
		for (Piece piece : pieces)
			piece.hide(user.getPlayer());
	}

	private Location getLocation(long time) {
		int secondsPerLap = getType().getSecondsPerLap();
		return navigator.getLocation(time % secondsPerLap / (double) secondsPerLap);
	}

}
