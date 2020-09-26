package museum.museum.subject;

import lombok.val;
import museum.data.SubjectInfo;
import museum.museum.Museum;
import museum.museum.map.FountainPrototype;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Color;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import ru.cristalix.core.util.UtilV3;

/**
 * @author func 18.09.2020
 * @project museum
 */
public class FountainSubject extends Subject {

	private static final int UPPER_ID_BOUND = -10_000;
	private static final int NEG_ID_BOUND = -100_000;

	private final Color colour;
	private final EntityFallingBlock entity;
	private final PacketPlayOutSpawnEntity spawn;
	private final PacketPlayOutEntityMetadata metadata;
	private final PacketPlayOutEntityDestroy destroy;

	public FountainSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		val acceptedPrototype = ((FountainPrototype) prototype);
		this.colour = Color.AQUA;
		val label = acceptedPrototype.getSource();
		val world = prototype.getBox().getWorld().getHandle();
		// То место, откуда вылетают частицы воды
		val acceptedSource = label.clone().subtract(prototype.getBox().getCenter()).add(UtilV3.toVector(info.location));
		// Блок, по примеру которого делается вода
		val icon = label.clone().subtract(0, 1, 0).getBlock();
		//noinspection deprecation
		entity = new EntityFallingBlock(
				world, acceptedSource.getX() + .5, acceptedSource.getY() + 1, acceptedSource.getZ() + .5,
				CraftMagicNumbers.getBlock(icon.getType()).fromLegacyData(icon.getData())
		);
		entity.id = NEG_ID_BOUND;

		// Создание пакетов
		spawn = new PacketPlayOutSpawnEntity(entity, 70, Block.getCombinedId(entity.block));
		metadata = new PacketPlayOutEntityMetadata(entity.id, entity.getDataWatcher(), true);
		destroy = new PacketPlayOutEntityDestroy(entity.id);
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
		if (!(user.getState() instanceof Museum))
			return;
		val connection = user.getConnection();
		entity.id = entity.id < UPPER_ID_BOUND ? ++entity.id : NEG_ID_BOUND;
		entity.ticksLived = 1;

		// Замена поля entity id во всех пакетах
		spawn.a = entity.id;
		metadata.a = entity.id;
		destroy.a[0] = entity.id - 5;

		// Отправка пакетов игроку
		connection.sendPacket(spawn);
		connection.sendPacket(metadata);
		connection.sendPacket(new PacketPlayOutEntityVelocity(
				entity.id,
				Math.random() / 10 - .05,
				.6,
				Math.random() / 10 - .05
		));
		connection.sendPacket(destroy);
	}
}
