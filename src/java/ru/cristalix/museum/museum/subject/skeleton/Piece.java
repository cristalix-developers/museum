package ru.cristalix.museum.museum.subject.skeleton;

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
	private final double deltaX;
	private final double deltaY;
	private final double deltaZ;
	private final double distanceSq;
	private final String name;
	private final float yaw;
	private final List<Piece> children = new ArrayList<>();

	// ToDo: Optimize via custom packets with pre-serialized byte data
	public Piece(EntityArmorStand as, Location worldOrigin) {
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
		this.deltaX = worldOrigin != null ? as.locX - worldOrigin.getX() : 0;
		this.deltaY = worldOrigin != null ? as.locY - worldOrigin.getY() : 0;
		this.deltaZ = worldOrigin != null ? as.locZ - worldOrigin.getZ() : 0;
		this.delta = new Vector(deltaX, deltaY, deltaZ);
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

	public void show(Player player, Location origin) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		syncSpawnPacket.c = origin.getX() + this.deltaX;
		syncSpawnPacket.d = origin.getY() + this.deltaY;
		syncSpawnPacket.e = origin.getZ() + this.deltaZ;
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
		packet.b = origin.getX() + this.deltaX;
		packet.c = origin.getY() + this.deltaY;
		packet.d = origin.getZ() + this.deltaZ;
		packet.e = (byte) ((int) ((origin.getYaw() + this.yaw) * 256.0F / 360.0F));
		packet.f = (byte) ((int) (origin.getPitch() * 256.0F / 360.0F));
		con.sendPacket(packet);
	}

}
