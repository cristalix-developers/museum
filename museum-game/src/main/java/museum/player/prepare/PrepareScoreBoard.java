package museum.player.prepare;

import lombok.val;
import org.bukkit.Bukkit;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;
import museum.App;
import museum.player.User;
import museum.util.LevelSystem;
import museum.util.MessageUtil;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareScoreBoard implements Prepare {

	private void template(SimpleBoardObjective objective) {
		objective.setDisplayName("Музей археологии");
		objective.startGroup("Сервер")
				.record("Онлайн", () -> Bukkit.getOnlinePlayers().size() + "")
				.record("total/max/free", () ->
						Runtime.getRuntime().totalMemory() / 1024 / 1024 + " " +
								Runtime.getRuntime().maxMemory() / 1024 / 1024 + " " +
								Runtime.getRuntime().freeMemory() / 1024 / 1024
				)
				.record("TPS", () -> String.format("%.2f", Bukkit.getTPS()[0]));
	}

	@Override
	public void execute(User user, App app) {
		SimpleBoardObjective main = IScoreboardService.get().getPlayerObjective(user.getUuid(), "main");
		main.startGroup("Игрок")
				.record("Уровень", () -> LevelSystem.formatExperience(user.getExperience()))
				.record("Баланс", () -> "§a" + MessageUtil.toMoneyFormat(user.getMoney()));
		val museum = user.getCurrentMuseum();
		main.startGroup("Музей")
				.record("Цена монеты", () -> "§b" + MessageUtil.toMoneyFormat(user.getCurrentMuseum().getIncome()))
				.record("Посещений", () -> "§b" + museum.getViews());
		template(main);

		SimpleBoardObjective excavation = IScoreboardService.get().getPlayerObjective(user.getUuid(), "excavation");
		excavation.startGroup("Раскопки")
				.record("Ударов", () -> Math.max(user.getExcavation() != null ? user.getExcavation().getHitsLeft() : 0, 0) + " осталось")
				.record("Уровень", () -> LevelSystem.formatExperience(user.getExperience()));
		template(excavation);

		IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");
	}

}
