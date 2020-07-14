package ru.cristalix.museum.visitor;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.App;

import java.util.ArrayList;
import java.util.List;

/**
 * @author func 14.07.2020
 * @project museum
 */
public class VisitorGroup {

	private final List<Visitor> visitors = new ArrayList<>();
	private int offset;

	public VisitorGroup(Location start, int amount, int offset) {
		this.offset = offset;

		double size = Math.sqrt(amount);

		for (int i = 0; i < amount; i++)
			visitors.add(new Visitor(start.getWorld()
					.spawnEntity(start, EntityType.VILLAGER), new V3(i / size, 0, i % size)));
	}

	public void move(Location meetingLocation) {
		visitors.forEach(visitor -> visitor.visit(meetingLocation));
	}

	public void clear() {
		visitors.clear();
	}
}
