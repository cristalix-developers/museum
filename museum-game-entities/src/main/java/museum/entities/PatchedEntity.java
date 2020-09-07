package museum.entities;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.function.Function;

public class PatchedEntity<T extends Entity> {

	public static final PatchedEntity<EntityVisitor> VISITOR = new PatchedEntity<>("villager", 120, EntityVisitor.class, EntityVisitor::new);

	private final Function<World, T> creator;

	PatchedEntity(String name, int id, Class<? extends Entity> custom, Function<World, T> creator) {
		EntityTypes.b.a(id, new MinecraftKey(name), custom);
		this.creator = creator;
	}

	public T spawn(Location loc) {
		return spawn(creator.apply(loc.getWorld()), loc);
	}

	public T spawn(T entity, Location loc) {
		entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		((CraftWorld) loc.getWorld()).getHandle().addEntity(entity);
		return entity;
	}

}
