package ru.cristalix.museum.player.prepare;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.GameMode;

/**
 * @author func 03.06.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum PrepareSteps {
	PACKET_HANDLER(new BeforePacketHandler()),
	INVENTORY(new PrepareInventory()),
	MUSEUM((user, app) -> user.getMuseums().get("main").load(user)),
	SCOREBOARD(new PrepareScoreBoard()),
	ANIME(new PrepareJSAnime()),
	HIDE_PLAYERS(new PreparePlayers()),
	GAMEMODE((user, app) -> user.getPlayer().setGameMode(GameMode.ADVENTURE));

	private final Prepare prepare;
}
