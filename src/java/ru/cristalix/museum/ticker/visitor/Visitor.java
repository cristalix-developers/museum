package ru.cristalix.museum.ticker.visitor;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import ru.cristalix.core.math.V3;

/**
 * @author func 09.06.2020
 * @project Museum
 */
@Getter
public class Visitor {

	private final EntityInsentient entity;
	private final V3 delta;

	public Visitor(org.bukkit.entity.Entity entity, V3 delta) {
		this.entity = (EntityInsentient) ((CraftEntity) entity).getHandle();
		this.entity.persistent = true;
		this.delta = delta;
	}
}
