package museum.museum.subject.skeleton;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Piece {

	private final int entityId;
	private final EntityArmorStand handle;
	private final PacketPlayOutEntityEquipment[] equipmentPackets;
	private final PacketPlayOutEntityMetadata metadataPacket;
	private final PacketPlayOutSpawnEntity syncSpawnPacket;
	private final PacketPlayOutEntityDestroy destroyPacket;
	private final Location worldOrigin;
	private final Vector delta;
	private final double distanceSq;
	private final String name;
	private final float yaw;
	private final List<Piece> children = new ArrayList<>();

	// ToDo: Optimize via custom packets with pre-serialized byte data
	public Piece(EntityArmorStand as, Location worldOrigin) {
		as.setBasePlate(false);
		as.setNoGravity(true);
		as.setInvisible(true);
		this.entityId = as.getId();
		this.handle = as;
		this.syncSpawnPacket = new PacketPlayOutSpawnEntity(as, 78);
		this.metadataPacket = new PacketPlayOutEntityMetadata(as.getId(), as.getDataWatcher(), false);
		this.destroyPacket = new PacketPlayOutEntityDestroy(as.getId());
		List<PacketPlayOutEntityEquipment> list = new ArrayList<>();
		for (EnumItemSlot slot : EnumItemSlot.values()) {
			ItemStack item = as.getEquipment(slot);
			if (item != null)
				list.add(new PacketPlayOutEntityEquipment(as.getId(), slot, item));
		}
		this.equipmentPackets = list.toArray(new PacketPlayOutEntityEquipment[0]);
		this.worldOrigin = worldOrigin;
		this.delta = new Vector(
				worldOrigin != null ? as.locX - worldOrigin.getX() : 0,
				worldOrigin != null ? as.locY - worldOrigin.getY() : 0,
				worldOrigin != null ? as.locZ - worldOrigin.getZ() : 0
		);
		this.distanceSq = delta.length();
		this.name = as.getCustomName();
		this.yaw = as.yaw;
	}

	public void link(Piece stand) {
		children.add(stand);
	}

	public double distanceSquared(Piece another) {
		return another.getDelta().subtract(this.getDelta()).length();
	}

	public List<Piece> getAllChildren() {
		return getChildrenRecursive(new ArrayList<>());
	}

	private List<Piece> getChildrenRecursive(List<Piece> buffer) {
		buffer.add(this);
		for (Piece child : children)
			child.getChildrenRecursive(buffer);
		return buffer;
	}

	public void show(Player player, Location origin, boolean inBlock) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		syncSpawnPacket.c = origin.getX() + (inBlock ? delta.getX() : delta.getX() % 1);
		syncSpawnPacket.d = origin.getY() + (inBlock ? delta.getY() : delta.getY() % 1);
		syncSpawnPacket.e = origin.getZ() + (inBlock ? delta.getZ() : delta.getZ() % 1);
		syncSpawnPacket.j = MathHelper.d((origin.getYaw() + this.yaw) * 256.0F / 360.0F);
		connection.sendPacket(syncSpawnPacket);
		connection.sendPacket(metadataPacket);
		for (PacketPlayOutEntityEquipment packet : equipmentPackets)
			connection.sendPacket(packet);
	}

	public void hide(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(destroyPacket);
	}

	public void update(Player player, Location origin) {
		PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
		packet.a = this.entityId;
		packet.b = origin.getX() + delta.getX();
		packet.c = origin.getY() + delta.getY();
		packet.d = origin.getZ() + delta.getZ();
		packet.e = (byte) ((int) ((origin.getYaw() + this.yaw) * 256.0F / 360.0F));
		packet.f = (byte) ((int) (origin.getPitch() * 256.0F / 360.0F));
		con.sendPacket(packet);
	}

}
