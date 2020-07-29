package museum.player.prepare;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import museum.prototype.Managers;

/**
 * @author func 03.06.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum PrepareSteps {
	PACKET_HANDLER(new BeforePacketHandler()),
	INVENTORY(new PrepareInventory()),
	ANIMATIONS(new PrepareJSAnime()),
	MUSEUM((user, app) -> user.getMuseums().get(Managers.museum.getPrototype("main")).show(user)),
	SCOREBOARD(new PrepareScoreBoard()),
	HIDE_PLAYERS((user, app) -> Bukkit.getOnlinePlayers().forEach(current -> user.getPlayer().hidePlayer(app, current))),
	GAMEMODE((user, app) -> user.getPlayer().setGameMode(GameMode.ADVENTURE));

	private final Prepare prepare;
}
