package museum.museum.subject.skeleton;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Piece implements Displayable {

	@Getter
	private final EntityArmorStand stand;
	private final PacketPlayOutEntityEquipment[] packetsEquipment;
	private final PacketPlayOutEntityMetadata packetMetadata;
	private final PacketPlayOutSpawnEntity packetSpawn;
	private final PacketPlayOutEntityDestroy packetDestroy;

	public Piece(EntityArmorStand as) {
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

	public void show(Player player, V4 pos) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		packetSpawn.c = pos.x;
		packetSpawn.d = pos.y;
		packetSpawn.e = pos.z;
		packetSpawn.j = MathHelper.d((pos.rot) * 256.0F / 360.0F);
		connection.sendPacket(packetSpawn);
		connection.sendPacket(packetMetadata);
		for (PacketPlayOutEntityEquipment packet : packetsEquipment)
			connection.sendPacket(packet);
	}

	public void hide(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetDestroy);
	}

	public void update(Player player, V4 pos) {
		PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
		packet.a = this.stand.id;
		packet.b = pos.x;
		packet.c = pos.y;
		packet.d = pos.z;
		packet.e = (byte) ((int) ((pos.rot + stand.yaw) * 256.0F / 360.0F));
		con.sendPacket(packet);
	}


}
