package ru.cristalix.museum.museum.subject;

import lombok.val;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockChange;
import org.bukkit.Location;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.player.pickaxe.Pickaxe;

/**
 * @author func 22.05.2020
 * @project Museum
 */
public class SimpleSubject implements Subject {

	private final SubjectPrototype prototype;
	protected final Museum museum;
	protected final SubjectInfo info;
	protected final Location location;

	public SimpleSubject(Museum museum, SubjectInfo info, SubjectPrototype prototype) {
		this.museum = museum;
		this.info = info;
		this.location = UtilV3.toLocation(info.getLocation(), App.getApp().getWorld());
		this.prototype = prototype;
	}

	@Override
	public SubjectType<?> getType() {
		return SubjectType.DECORATION;
	}

	@Override
	public void show(User owner) {
		update(owner, false);
	}

	@Override
	public void hide(User owner) {
		update(owner, true);
	}

	private void update(User user, boolean hide) {
		val start = prototype.getBox().getMin();
		val world = App.getApp().getNMSWorld();
		V3 dims = prototype.getBox().getDimensions();

		for (int x = 0; x <= dims.getX(); x++) {
			for (int y = 0; y <= dims.getY(); y++) {
				for (int z = 0; z <= dims.getZ(); z++) {
					val source = new BlockPosition(
							start.x + x,
							start.y + y,
							start.z + z
					);
					val destination = new BlockPosition(
							location.x + x,
							location.y + y,
							location.z + z
					);
					val packet = new PacketPlayOutBlockChange(world, destination);
					packet.block = hide ? Pickaxe.AIR_DATA : world.getType(new BlockPosition(source));
					user.sendPacket(packet);
				}
			}
		}
	}

	@Override
	public SubjectInfo generateInfo() {
		return info;
	}

}
