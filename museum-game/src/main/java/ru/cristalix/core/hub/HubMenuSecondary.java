package ru.cristalix.core.hub;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.cristalix.core.CoreApi;
import ru.cristalix.core.permissions.IPermissionService;
import ru.cristalix.core.realm.IRealmService;
import ru.cristalix.core.realm.RealmId;
import ru.cristalix.core.realm.RealmInfo;

import static ru.cristalix.core.hub.ScriptHubUtils.hasAccess;

@Getter
public class HubMenuSecondary implements HubMenu {

	private final SimpleBukkitHubItem icon;

	public HubMenuSecondary(HubItem icon) {
		this.icon = new SimpleBukkitHubItem(icon);
	}

	public static HubMenuSecondary virtual(String realmType, Material icon, int data, String... description) {
		return new HubMenuSecondary(new SimpleBukkitHubItem(realmType, icon, data, description));
	}

	@Override
	public String getRealmType() {
		return icon.getRealmType();
	}

	@Override
	public void handleClick(ScriptHubService service, Player player, String icon) {
		if (icon.equals("CLOSE")) {
			service.openMenu(player, service.getMainMenu());
			return;
		}

		RealmId realmId = RealmId.of(icon);
		if (!hasAccess(player, realmId)) return;
		RealmInfo info = IRealmService.get().getRealmById(realmId);
		if (info == null) return;
		HubUtil.transfer(IPermissionService.get(), CoreApi.get().getSocketClient(), player, info);
	}

	@Override
	public HubServerInfo[] reloadInfo() {
		return new HubServerInfo[0];
	}

}
