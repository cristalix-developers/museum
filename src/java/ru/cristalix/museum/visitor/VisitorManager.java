package ru.cristalix.museum.visitor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import ru.cristalix.museum.App;
import ru.cristalix.museum.player.pickaxe.Pickaxe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class VisitorManager {

	private final List<Location> node;
	private final List<VisitorGroup> groups;

	public VisitorManager(int visitorCount, int groupCount) {
		node = new ArrayList<>();
		groups = new ArrayList<>();
		// todo: do after
	}

	private void spawnGroup(int visitorCount) {
		groups.add(new VisitorGroup(node.get(0), visitorCount, groups.size()));
	}

	public void clear() {
		groups.forEach(VisitorGroup::clear);
		App.getApp().getWorld().getEntities().stream()
				.filter(entity -> entity.getType() == EntityType.VILLAGER)
				.forEach(Entity::remove);
	}
}
