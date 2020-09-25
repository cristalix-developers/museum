package museum.museum.subject;

import lombok.val;
import museum.data.SubjectInfo;
import museum.museum.Museum;
import museum.museum.map.FountainPrototype;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import ru.cristalix.core.util.UtilV3;

/**
 * @author func 18.09.2020
 * @project museum
 */
public class FountainSubject extends Subject {

	private final Color colour;
	private final EntityFallingBlock entity;

	public FountainSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		val acceptedPrototype = ((FountainPrototype) prototype);
		this.colour = Color.AQUA;
		val acceptedSource = acceptedPrototype.getSource().clone().subtract(prototype.getBox().getMin()).add(UtilV3.toVector(info.location));
		val location = acceptedPrototype.getSource().clone().subtract(0, 1, 0);
		System.out.println(location.getBlock().getType());
		val world = prototype.getBox().getWorld().getHandle();
		this.entity = new EntityFallingBlock(
				world, acceptedSource.getX(), acceptedSource.getY(), acceptedSource.getZ(),
				world.c(new BlockPosition(location.getX(), location.getY(), location.getZ()))
		);
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
			entity.id = (int) (10_000_000 * Math.random()) + 1_000;
			entity.ticksLived = 1;
			connection.sendPacket(new PacketPlayOutSpawnEntity(entity, 70));
			connection.sendPacket(new PacketPlayOutEntityMetadata(entity.id, entity.getDataWatcher(), false));
			connection.sendPacket(new PacketPlayOutEntityVelocity(entity.id, 0, .4, 0));
		}
	}
}
