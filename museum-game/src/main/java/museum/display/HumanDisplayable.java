package museum.display;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import museum.App;
import museum.player.User;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public class HumanDisplayable implements Displayable {

	private final EntityPlayer npcEntity;
	@Getter
	private final Consumer<User> onInteract;

	public HumanDisplayable(String skinUrl, String displayName, Consumer<User> onInteract) {
		this.onInteract = onInteract;
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");

		gameProfile.getProperties().put("textures", new Property("skinURL", skinUrl, ""));
		gameProfile.getProperties().put("textures", new Property("skinDigest", skinUrl.substring(skinUrl.length() - 10), ""));

		this.npcEntity = new EntityPlayer(
				((CraftServer) Bukkit.getServer()).getServer(),
				null,
				gameProfile,
				new PlayerInteractManager(App.getApp().getNMSWorld())
		);
		this.npcEntity.displayName = displayName;

		Player player = this.npcEntity.getBukkitEntity().getPlayer();
		player.setPlayerListName(displayName);

		((CraftPlayer) player).getHandle().ping = -2;

	}

//	public void lookAt(Location target) {
//		location.setYaw((float) toDegrees(atan2(location.x - target.x, target.z - location.z)));
//		location.setPitch((float) toDegrees(atan2(location.y - target.y, sqrt(square(location.x - target.x) + square(location.z - target.z)))));
//	}

	@Override
	public void getShowPackets(Collection<Packet<PacketListenerPlayOut>> buffer, V5 position) {
		this.npcEntity.setLocation(position.getX(), position.getY(), position.getZ(),
				(int) (position.getYaw() * 256F / 360F), (int) (position.getPitch() * 256F / 360F));

		DataWatcher dataWatcher = this.npcEntity.getDataWatcher();
		dataWatcher.set(EntityHuman.br, (byte) 127);

		buffer.addAll(Arrays.asList(
				new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this.npcEntity),
				new PacketPlayOutNamedEntitySpawn(this.npcEntity),
				new PacketPlayOutEntityMetadata(this.npcEntity.getId(), dataWatcher, true),
				new PacketPlayOutEntityHeadRotation(npcEntity, (byte) ((int) (position.getYaw() * 256F / 360F))),
				new PacketPlayOutEntity.PacketPlayOutEntityLook(npcEntity.getId(), (byte) position.getYaw(), (byte) position.getPitch(), true)
								   ));
	}

	@Override
	public void getHidePackets(Collection<Packet<PacketListenerPlayOut>> buffer) {
		buffer.add(new PacketPlayOutEntityDestroy(npcEntity.id));
	}

	@Override
	public void getUpdatePackets(Collection<Packet<PacketListenerPlayOut>> buffer, V5 position) {
		PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook pckg = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook();
		byte yaw = (byte) ((int) (position.getYaw() * 256.0F / 360.0F));
		pckg.a = npcEntity.id;
		pckg.e = yaw;
		pckg.f = (byte) ((int) (position.getPitch() * 256.0F / 360.0F));
		pckg.g = true;
		pckg.h = false;
		buffer.add(pckg);
		PacketPlayOutEntityHeadRotation rot = new PacketPlayOutEntityHeadRotation();
		rot.a = npcEntity.id;
		rot.b = yaw;
		buffer.add(rot);
	}

}
