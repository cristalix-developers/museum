package ru.cristalix.core.hub;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.val;
import org.bukkit.entity.Player;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.realm.IRealmService;
import ru.cristalix.core.realm.RealmInfo;
import ru.cristalix.core.util.UtilLegacy;
import ru.cristalix.core.util.UtilNetty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HubMenuMain implements HubMenu {

	private final Map<String, HubMenuSecondary> children;

	@Getter
	private final byte[] setupData;

	public HubMenuMain(List<HubMenuSecondary> children) {

		this.children = children.stream().collect(Collectors.toMap(HubMenu::getRealmType, v -> v));

		ByteBuf buffer = Unpooled.buffer();
		buffer.writeInt(this.children.size());
		this.children.forEach((type, menu) -> {
			UtilNetty.writeString(buffer, type);
			buffer.writeBytes(menu.getIcon().getEncodedData());
		});
		this.setupData = buffer.array();

	}

	@Override
	public String getRealmType() {
		return "";
	}

	@Override
	public HubServerInfo[] reloadInfo() {
		return children.keySet().stream().map(ScriptHubUtils::hubInfoFromType).toArray(HubServerInfo[]::new);
	}

	@Override
	public void handleClick(ScriptHubService service, Player player, String icon) {

		if (icon.equals("CLOSE")) {
			service.openMenu(player, null);
			return;
		}

		val child = children.get(icon);
		if (child == null) return;
		
		val infos = IRealmService.get().getStreamRealmsOfType(icon)
				.filter(ri -> ri.getRealmId().getId() > 0)
				.collect(Collectors.toList());

		if (infos.isEmpty()) return;

		
		if (infos.stream().anyMatch(RealmInfo::isLobbyServer)) {
			val freestLobby = UtilLegacy.findBestFreeServer(infos);
			if (freestLobby != null) {
				HubUtil.transfer(IPermissionService.get(), CoreApi.get().getSocketClient(), player, freestLobby);
			}
			return;
		}
		
		service.openMenu(player, child);
	}

}
