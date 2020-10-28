package museum.worker;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import museum.App;
import museum.museum.subject.skeleton.Displayable;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.Math.*;
import static org.bukkit.util.NumberConversions.square;

/**
 * @author func 16.09.2020
 * @project museum
 */
public class NpcWorker implements Displayable {

	@Getter
	@Setter
	private Location location;
	private final EntityPlayer npcEntity;
	@Getter
	private final Consumer<User> onInteract;
	private final Map<String, Object> metadata = Maps.newHashMap();

	public NpcWorker(Location location, String skinUrl, String displayName, Consumer<User> onInteract) {
		this.location = location;
		this.onInteract = onInteract;
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");

		gameProfile.getProperties().put("textures", new Property("skinURL", skinUrl, ""));
		gameProfile.getProperties().put("textures", new Property("skinDigest", skinUrl.substring(skinUrl.length() - 10), ""));

		this.npcEntity = new EntityPlayer(
				((CraftServer) Bukkit.getServer()).getServer(),
				location,
				gameProfile,
				new PlayerInteractManager(App.getApp().getNMSWorld())
		);
		this.npcEntity.displayName = displayName;
		this.npcEntity.setLocation(location.getX(), location.getY(), location.getZ(), (int) (location.getYaw() * 256F / 360F), (int) (location.getPitch() * 256F / 360F));

		Player player = this.npcEntity.getBukkitEntity().getPlayer();
		player.setPlayerListName(displayName);

		player.teleport(location);
		((CraftPlayer) player).getHandle().ping = -2;

		setMeta("id", npcEntity.id);
	}

	public void setMeta(String key, Object value) {
		metadata.put(key, value);
	}

	public <T> T getMeta(String key) {
		return (T) metadata.getOrDefault(key, null);
	}

	@Override
	public void getShowPackets(Collection<Packet<PacketListenerPlayOut>> buffer, V4 position) {
		this.npcEntity.setLocation(location.getX(), location.getY(), location.getZ(), (int) (location.getYaw() * 256F / 360F), (int) (location.getPitch() * 256F / 360F));

		DataWatcher dataWatcher = this.npcEntity.getDataWatcher();
		dataWatcher.set(EntityHuman.br, (byte) 127);

		buffer.addAll(Arrays.asList(
				new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this.npcEntity),
				new PacketPlayOutNamedEntitySpawn(this.npcEntity),
				new PacketPlayOutEntityMetadata(this.npcEntity.getId(), dataWatcher, true),
				new PacketPlayOutEntityHeadRotation(npcEntity, (byte) ((int) (location.getYaw() * 256F / 360F))),
				new PacketPlayOutEntity.PacketPlayOutEntityLook(npcEntity.getId(), (byte) location.getYaw(), (byte) location.getPitch(), true)
		));
	}

	@Override
	public void getHidePackets(Collection<Packet<PacketListenerPlayOut>> buffer) {
		buffer.add(new PacketPlayOutEntityDestroy(npcEntity.id));
	}

	@Override
	public void getUpdatePackets(Collection<Packet<PacketListenerPlayOut>> buffer, V4 position) {
		Location target = position.toLocation(App.getApp().getWorld());
		// Если игрок и НПС слишком далеко, то не нужно поворачивать ему голову
		if (location.distanceSquared(target) > 325)
			return;
		// Поворот головы НПС для игрока
		PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook pckg = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook();
		float yawDeg = (float) toDegrees(atan2(location.getX() - target.getX(), target.getZ() - location.getZ()));
		byte yaw = (byte) ((int) (yawDeg * 256.0F / 360.0F));
		pckg.a = npcEntity.id;
		pckg.e = yaw;
		float pitchDeg = (float) toDegrees(atan2(location.getY() - target.getY(), sqrt(square(location.getX() - target.getX()) + square(location.getZ() - target.getZ()))));
		pckg.f = (byte) ((int) (pitchDeg * 256.0F / 360.0F));
		pckg.g = true;
		pckg.h = false;
		buffer.add(pckg);
		PacketPlayOutEntityHeadRotation rot = new PacketPlayOutEntityHeadRotation();
		rot.a = npcEntity.id;
		rot.b = yaw;
		buffer.add(rot);
	}
}
