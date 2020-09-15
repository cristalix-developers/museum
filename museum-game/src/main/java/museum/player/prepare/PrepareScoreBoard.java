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
		long megabyte = 1024 * 1024;
		Runtime runtime = Runtime.getRuntime();

		objective.startGroup("Сервер")
				.record("Онлайн", () -> Bukkit.getOnlinePlayers().size() + "")
				.record("total/max/free", () ->
						runtime.totalMemory() / megabyte + " " +
								runtime.maxMemory() / megabyte + " " +
								runtime.freeMemory() / megabyte
				).record("TPS", () -> String.format("%.2f", Bukkit.getTPS()[0]));
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
		SimpleBoardObjective excavation = IScoreboardService.get().getPlayerObjective(user.getUuid(), "excavation");

		IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");
	}
}
