package ru.cristalix.museum.visitor;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import ru.cristalix.core.math.V3;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class Visitor {

	@Getter
	private final EntityInsentient entity;
	private final V3 offset;

	public Visitor(org.bukkit.entity.Entity entity, V3 offset) {
		this.entity = (EntityInsentient) ((CraftEntity) entity).getHandle();
		this.offset = offset;
	}

	public void visit(Location meetingLocation) {
		entity.ticksLived = 0;
		entity.getNavigation().a(
				meetingLocation.getX() + offset.getX(),
				meetingLocation.getY() + offset.getY(),
				meetingLocation.getZ() + offset.getZ(),
				.6
		);
	}

}
