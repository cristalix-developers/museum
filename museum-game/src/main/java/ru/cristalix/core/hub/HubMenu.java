package ru.cristalix.core.hub;

import org.bukkit.entity.Player;

public interface HubMenu {

	String getRealmType();

	void handleClick(ScriptHubService service, Player player, String icon);

	HubServerInfo[] reloadInfo();

}
