package ru.cristalix.museum.visitor;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import ru.cristalix.museum.App;

import java.util.ArrayList;
import java.util.List;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class VisitorManager {

	private final List<Location> node;
	private final List<VisitorGroup> groups;
	private final int visitorInGroup;
	private final int groupCount;
	private int wait = 0;

	public VisitorManager(int groupCount, int visitorInGroup) {
		node = new ArrayList<>();
		groups = new ArrayList<>();
		this.groupCount = groupCount;
		this.visitorInGroup = visitorInGroup;
	}

	private void update() {
		if (wait > 0)
			wait--;
		if (groups.size() < groupCount && wait == 0) {
			wait = 30;
			groups.add(new VisitorGroup(node.get(0), visitorInGroup));
		}
		//groups.forEach(group -> group.move());
	}

	public void clear() {
		groups.forEach(VisitorGroup::clear);
		App.getApp().getWorld().getEntities().stream()
				.filter(entity -> entity.getType() == EntityType.VILLAGER)
				.forEach(Entity::remove);
	}
}
