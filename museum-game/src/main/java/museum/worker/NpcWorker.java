package museum.worker;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.val;
import museum.App;
import museum.player.User;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author func 16.09.2020
 * @project museum
 */
public class NpcWorker {

	@Getter
	private final Location location;
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

	public void show(User user) {
		val connection = user.getConnection();

		connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this.npcEntity));
		connection.sendPacket(new PacketPlayOutNamedEntitySpawn(this.npcEntity));

		DataWatcher dataWatcher = this.npcEntity.getDataWatcher();
		dataWatcher.set(EntityHuman.br, (byte) 127);

		connection.sendPacket(new PacketPlayOutEntityMetadata(this.npcEntity.getId(), dataWatcher, true));

		connection.sendPacket(new PacketPlayOutEntityHeadRotation(npcEntity, (byte) ((int) (location.getYaw() * 256F / 360F))));
		connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(npcEntity.getId(), (byte) location.getYaw(), (byte) location.getPitch(), true));
	}

	public void hide(User user) {
		user.sendPacket(new PacketPlayOutEntityDestroy(npcEntity.id));
	}
}
