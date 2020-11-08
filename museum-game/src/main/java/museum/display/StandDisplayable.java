package museum.display;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StandDisplayable implements Displayable, Piece {

	@Getter
	private final EntityArmorStand stand;
	private final PacketPlayOutEntityEquipment[] packetsEquipment;
	private final PacketPlayOutEntityMetadata packetMetadata;
	private final PacketPlayOutSpawnEntity packetSpawn;
	private final PacketPlayOutEntityDestroy packetDestroy;

	public StandDisplayable(EntityArmorStand as) {
		this.stand = as;

		as.setBasePlate(false);
		as.setNoGravity(true);
		as.setInvisible(true);
		as.setCustomNameVisible(false);

		this.packetSpawn = new PacketPlayOutSpawnEntity(as, 78);
		this.packetMetadata = new PacketPlayOutEntityMetadata(as.getId(), as.getDataWatcher(), false);
		this.packetDestroy = new PacketPlayOutEntityDestroy(as.getId());
		List<PacketPlayOutEntityEquipment> list = new ArrayList<>();
		for (EnumItemSlot slot : EnumItemSlot.values()) {
			ItemStack item = as.getEquipment(slot);
			if (item != null)
				list.add(new PacketPlayOutEntityEquipment(as.getId(), slot, item));
		}
		this.packetsEquipment = list.toArray(new PacketPlayOutEntityEquipment[0]);
	}

	@Override
	public Location getLocation() {
		return new Location(stand.getWorld().getWorld(), stand.locX, stand.locY, stand.locZ);
	}

	@Override
	public void getShowPackets(Collection<Packet<PacketListenerPlayOut>> buffer, V5 position) {
		packetSpawn.c = position.x;
		packetSpawn.d = position.y;
		packetSpawn.e = position.z;
		packetSpawn.j = MathHelper.d((position.yaw) * 256.0F / 360.0F);
		buffer.add(packetSpawn);
		buffer.add(packetMetadata);
		buffer.addAll(Arrays.asList(packetsEquipment));
	}

	@Override
	public void getUpdatePackets(Collection<Packet<PacketListenerPlayOut>> buffer, V5 position) {
		PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
		packet.a = this.stand.id;
		packet.b = position.x;
		packet.c = position.y;
		packet.d = position.z;
		packet.e = (byte) ((int) ((position.yaw + stand.yaw) * 256.0F / 360.0F));
		buffer.add(packet);
	}

	@Override
	public void getHidePackets(Collection<Packet<PacketListenerPlayOut>> buffer) {
		buffer.add(packetDestroy);
	}

}
