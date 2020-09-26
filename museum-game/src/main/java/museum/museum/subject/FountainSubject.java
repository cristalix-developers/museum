package museum.museum.subject;

import lombok.val;
import museum.data.SubjectInfo;
import museum.museum.Museum;
import museum.museum.map.FountainPrototype;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import ru.cristalix.core.util.UtilV3;

/**
 * @author func 18.09.2020
 * @project museum
 */
public class FountainSubject extends Subject {

	private final Color colour;
	private final EntityFallingBlock entity;
	private final PacketPlayOutSpawnEntity spawn;
	private final PacketPlayOutEntityMetadata metadata;
	private final PacketPlayOutEntityDestroy destroy;

	@SuppressWarnings("deprecation")
	public FountainSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		val acceptedPrototype = ((FountainPrototype) prototype);
		this.colour = Color.AQUA;
		val label = acceptedPrototype.getSource();
		val world = prototype.getBox().getWorld().getHandle();
		val acceptedSource = label.clone().subtract(prototype.getBox().getCenter()).add(UtilV3.toVector(info.location));
		val icon = label.clone().subtract(0, 1, 0).getBlock();
		this.entity = new EntityFallingBlock(
				world, acceptedSource.getX() + .5, acceptedSource.getY() + 1, acceptedSource.getZ() + .5,
				CraftMagicNumbers.getBlock(icon.getType()).fromLegacyData(icon.getData())
		);
		this.entity.id = -100000;
		spawn = new PacketPlayOutSpawnEntity(entity, 70, Block.getCombinedId(entity.block));
		metadata = new PacketPlayOutEntityMetadata(entity.id, entity.getDataWatcher(), true);
		destroy = new PacketPlayOutEntityDestroy(-100000);
	}

	@Override
	public void updateInfo() {
		super.updateInfo();
	}

	@Override
	public void setAllocation(Allocation allocation) {
		super.setAllocation(allocation);
	}

	public void throwWater(User user) {
		if (user.getState() instanceof Museum) {
			val connection = user.getConnection();
			entity.id = entity.id < -1000 ? ++entity.id : -100000;
			entity.ticksLived = 1;
			spawn.a = entity.id;
			metadata.a = entity.id;
			destroy.a[0] = entity.id - 5;
			connection.sendPacket(spawn);
			connection.sendPacket(metadata);
			connection.sendPacket(new PacketPlayOutEntityVelocity(entity.id, Math.random() / 10 - .05, .6, Math.random() / 10 - .05));
			connection.sendPacket(destroy);
		}
	}
}
