package museum.visitor;

import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

import java.util.function.Function;

public class PatchedEntity<T extends EntityVillager> {

	public static final PatchedEntity<EntityVisitor> VISITOR = new PatchedEntity<>("villager", 120);

	PatchedEntity(String name, int id) {
		EntityTypes.b.a(id, new MinecraftKey(name), EntityVisitor.class);
	}

	public T spawn(T entity, Location loc) {
		entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		((CraftWorld) loc.getWorld()).getHandle().addEntity(entity);
		entity.r();
		return entity;
	}

}
