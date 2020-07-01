package ru.cristalix.museum.museum.subject;

import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockChange;
import org.bukkit.Location;
import ru.cristalix.core.util.UtilV3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.player.User;

public class MarkerSubject extends Subject {

	private final Location location;

	@Getter
	private final int collectorId;

	public MarkerSubject(SubjectPrototype prototype, SubjectInfo info, User user) {
		super(prototype, info, user);
		this.location = UtilV3.toLocation(info.getLocation().clone().add(0.5, 0, 0.5), App.getApp().getWorld());
		this.collectorId = info.getMetadata() == null ? 0 : Integer.parseInt(info.getMetadata());
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public void show(User user) {
		val packet = new PacketPlayOutBlockChange(App.getApp().getNMSWorld(), new BlockPosition(location.x, location.y, location.z));
		packet.block = Blocks.REDSTONE_TORCH.blockData;
		user.sendPacket(packet);
	}

	@Override
	public void hide(User user, boolean visually) {
		val packet = new PacketPlayOutBlockChange(App.getApp().getNMSWorld(), new BlockPosition(location.x, location.y, location.z));
		packet.block = Blocks.AIR.blockData;
		user.sendPacket(packet);
	}

	@Override
	public void updateInfo() {
		cachedInfo.metadata = String.valueOf(collectorId);
	}

}
