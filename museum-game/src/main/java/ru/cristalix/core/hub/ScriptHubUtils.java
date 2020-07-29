package ru.cristalix.core.hub;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.cristalix.core.permissions.IGroup;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.realm.IRealmService;
import ru.cristalix.core.realm.RealmId;
import ru.cristalix.core.realm.RealmInfo;
import ru.cristalix.core.realm.RealmStatus;
import ru.cristalix.core.util.UtilNetty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static ru.cristalix.core.permissions.StaffGroups.*;

@UtilityClass
public class ScriptHubUtils {

	public static void sendPayload(Player p, String channel, String string) {
		ByteBuf buffer = Unpooled.buffer();
		UtilNetty.writeString(buffer, string);
		sendPayload(p, channel, buffer);
	}

	public static void sendPayload(Player p, String channel, ByteBuf buffer) {
		PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(channel, new PacketDataSerializer(buffer));
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		buffer.release();
	}

	public static boolean hasAccess(Player player, RealmId realmId) {

		IPermissionService permissionService = IPermissionService.get();

		boolean testServer = realmId.getId() <= 0;
		boolean buildServer = realmId.getRealmName().equals("BUIL");
		if (!testServer && !buildServer) return true;

		IGroup group = permissionService.getPermissionContextDirect(player.getUniqueId()).getStaffGroup();
		boolean isBuilder = group == BUILDER || group == SR_BUILDER || group == CUR_BUILDER;
		boolean isAdmin = group.getPriority() >= ADMIN.getPriority();

		return isBuilder && buildServer || isAdmin;

	}

	public static HubServerInfo hubInfoFromRealm(RealmInfo info) {
		RealmId id = info.getRealmId();
		return new HubServerInfo(id.getTypeName(), id.getId(), info.getCurrentPlayers(), info.getMaxPlayers(), info.getExtraSlots(), info.getStatus());
	}

	public static HubServerInfo hubInfoFromType(String realmType) {
		int online = 0, max = 0, extra = 0;
		for (RealmInfo realm : getServicedTree(RealmId.of(realmType, 0))) {
			online += realm.getCurrentPlayers();
			max += realm.getMaxPlayers();
			extra += realm.getExtraSlots();
		}
		RealmStatus status = max > 0 ? RealmStatus.WAITING_FOR_PLAYERS : RealmStatus.STARTING_GAME;
		return new HubServerInfo(realmType, 0, online, max, extra, status);
	}

	private static RealmInfo[] getServicedRealms(IRealmService service, RealmId address) {
		if (address.getId() != 0) {
			RealmInfo realm = service.getRealmById(address);
			if (realm == null) return null;
			return Arrays.stream(realm.getServicedServers())
					.map(RealmId::of)
					.map(service::getRealmById)
					.toArray(RealmInfo[]::new);
		}

		String type = address.getTypeName();
		Predicate<RealmInfo> filter = type.equals("TECHNICAL") ?
				info -> info.getRealmId().getId() < 0 : // Display all TEST-realms for TECHNICAL menu
				info -> info.getRealmId().getTypeName().equals(type) && info.getRealmId().getId() > 0; // Do not display TEST-realms for other menus

		return service.getRealms().stream()
				.filter(filter)
				.toArray(RealmInfo[]::new);
	}

	public static Set<RealmInfo> getServicedTree(RealmId id) {
		Set<RealmInfo> set = new HashSet<>();
		recursiveResolve(IRealmService.get(), id, set);
		return set;
	}

	private static void recursiveResolve(IRealmService service, RealmId id, Set<RealmInfo> buffer) {
		RealmInfo[] servicedRealms = getServicedRealms(service, id);
		if (servicedRealms == null) return;
		for (RealmInfo servicedRealm : servicedRealms) {
			if (buffer.add(servicedRealm)) recursiveResolve(service, servicedRealm.getRealmId(), buffer);
		}
	}

}
