package museum.museum.subject;

import clepto.bukkit.B;
import lombok.val;
import museum.data.SubjectInfo;
import museum.museum.Museum;
import museum.museum.map.FountainPrototype;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import museum.util.Colorizer;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import ru.cristalix.core.util.UtilV3;

/**
 * @author func 18.09.2020
 * @project museum
 */
public class FountainSubject extends Subject {

	private static final int UPPER_ID_BOUND = -900;
	private static final int NEG_ID_BOUND = -1_000;

	private EntityFallingBlock entity;
	private PacketPlayOutSpawnEntity spawn;
	private PacketPlayOutEntityMetadata metadata;
	private PacketPlayOutEntityDestroy destroy;

	private Location spawnLocation;

	public FountainSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
	}

	@Override
	public void updateInfo() {
		super.updateInfo();
	}

	@Override
	public double getIncome() {
		return ((Museum) getAllocation().getState()).getIncome()*0.05;
	}

	@Override
	public void setAllocation(Allocation allocation) {
		super.setAllocation(allocation);
		if (cachedInfo == null || cachedInfo.location == null || prototype == null || allocation == null)
			return;
		val acceptedPrototype = ((FountainPrototype) prototype);
		val label = acceptedPrototype.getSource();
		val world = prototype.getBox().getWorld().getHandle();
		// То место, откуда вылетают частицы воды
		val acceptedSource = label.clone()
				.subtract(prototype.getBox().getCenter())
				.add(UtilV3.toVector(cachedInfo.location));
		// Блок, по примеру которого делается вода
		val icon = label.clone().subtract(0, 1, 0).getBlock();
		spawnLocation = new Location(
				prototype.getBox().getWorld(),
				acceptedSource.getX() + .5,
				acceptedSource.getY() + 1,
				acceptedSource.getZ() + .5
		);
		entity = new EntityFallingBlock(
				world, spawnLocation.getX(),  spawnLocation.getY(),  spawnLocation.getZ(),
				Colorizer.applyColor(CraftMagicNumbers.getBlock(icon.getType()).getBlockData(), cachedInfo.getColor())
		);
		entity.id = NEG_ID_BOUND;

		// Создание пакетов
		spawn = new PacketPlayOutSpawnEntity(entity, 70, Block.getCombinedId(entity.block));
		metadata = new PacketPlayOutEntityMetadata(entity.id, entity.getDataWatcher(), true);
		destroy = new PacketPlayOutEntityDestroy(new int[]{entity.id});
	}

	public void throwWater() {
		if (!(owner.getState() instanceof Museum) || getAllocation() == null || entity == null)
			return;

		// Если хозяин не в своем музее, то не запускать воду
		if (!((Museum) owner.getState()).getOwner().equals(owner))
			return;

		entity.id = entity.id < UPPER_ID_BOUND ? ++entity.id : NEG_ID_BOUND;
		entity.ticksLived = 1;

		// Замена поля entity id во всех пакетах
		val oldId = entity.id;
		spawn.a = entity.id;
		metadata.a = entity.id;

		// Отправка пакетов игроку
		val users = owner.getState().getUsers();
		for (User visitor : users) {
			if (visitor.getPlayer() == null)
				continue;
			// Если игрок далеко
			if (visitor.getLocation().distanceSquared(spawnLocation) > 500)
				continue;

			visitor.sendPacket(spawn);
			visitor.sendPacket(metadata);
			visitor.sendPacket(new PacketPlayOutEntityVelocity(
					entity.id,
					Math.random() / 10 - .05,
					.7,
					Math.random() / 10 - .05
			));
		}
		B.postpone(30, () -> {
			destroy.a[0] = oldId;
			for (User visitor : users) {
				if (visitor.getPlayer() == null)
					continue;
				visitor.sendPacket(destroy);
			}
		});
	}
}
