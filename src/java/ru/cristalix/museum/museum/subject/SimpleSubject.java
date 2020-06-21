package ru.cristalix.museum.museum.subject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.player.User;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SimpleSubject implements Subject {

	protected final SubjectPrototype prototype;
	protected final Museum museum;
	protected final SubjectInfo info;
	protected final Location location;

	private static final IBlockData AIR = Block.getByCombinedId(0);

	public SimpleSubject(Museum museum, SubjectInfo info) {
		this.museum = museum;
		this.info = info;
		V3 loc = info.getLocationDelta();
		this.location = museum.getPrototype().getOrigin().clone().add(loc.getX(), loc.getY(), loc.getZ());
		this.prototype = museum.getPrototype().getMap().getSubjectPrototype(info.prototypeAddress);
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
		val start = prototype.getPointMin();
		val end = prototype.getPointMax();
		val world = App.getApp().getNMSWorld();
		for (int y = start.getBlockY(); y <= end.getBlockY(); y++) {
			// x in pointMin MUST be less than z
			for (int x = start.getBlockX(); x <= end.getBlockX(); x++) {
				for (int z = start.getBlockZ(); z <= end.getBlockZ(); z++) {
					// todo проверить правильность координат
					val blockPosition = new BlockPosition(x, y, z);
					val packet = new PacketPlayOutBlockChange(world, blockPosition);
					packet.block = hide ? AIR : world.getType(blockPosition);
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
