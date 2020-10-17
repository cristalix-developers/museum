package museum.player.prepare;

import clepto.cristalix.Cristalix;
import museum.App;
import museum.player.User;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import org.bukkit.Bukkit;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;

import java.util.UUID;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareScoreBoard implements Prepare {

	public static void addUserInfo(SimpleBoardObjective objective, User user) {
		objective.startGroup("Игрок")
				.record("Уровень", () -> LevelSystem.formatExperience(user.getExperience()))
				.record("Баланс", () -> "§a" + MessageUtil.toMoneyFormat(user.getMoney()));
	}

	public static void addServerInfo(SimpleBoardObjective objective) {
		objective.startGroup("Сервер")
				.record("Онлайн", () -> Bukkit.getOnlinePlayers().size() + "")
				.record("TPS", () -> String.format("%.2f", Bukkit.getTPS()[0]));
	}

	public static void setupScoreboard(User user) {
		String address = UUID.randomUUID().toString();
		SimpleBoardObjective objective = Cristalix.scoreboardService().getPlayerObjective(user.getUuid(), address);
		addUserInfo(objective, user);
		user.getState().setupScoreboard(user, objective);
		addServerInfo(objective);
		Cristalix.scoreboardService().setCurrentObjective(user.getUuid(), address);
	}

	@Override
	public void execute(User user, App app) {
		IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");
	}
}
