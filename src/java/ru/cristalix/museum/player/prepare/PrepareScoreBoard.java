package ru.cristalix.museum.player.prepare;

import lombok.val;
import org.bukkit.Bukkit;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;
import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.util.Levels;
import ru.cristalix.museum.util.MessageUtil;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareScoreBoard implements Prepare {

	private void template(SimpleBoardObjective objective) {
		objective.setDisplayName("Музей археологии");
		objective.startGroup("Сервер")
				.record("Онлайн", () -> Bukkit.getOnlinePlayers().size() + "")
				.record("Свободно", () -> Runtime.getRuntime().freeMemory() / 1024 / 1024 + "мб")
				.record("TPS", () -> String.format("%.2f", Bukkit.getTPS()[0]));
	}

	@Override
	public void execute(User user, App app) {
		SimpleBoardObjective main = IScoreboardService.get().getPlayerObjective(user.getUuid(), "main");
		main.startGroup("Игрок")
				.record("Уровень", () -> Levels.formatExperience(user.getExperience()))
				.record("Баланс", () -> "§a" + MessageUtil.toMoneyFormat(user.getMoney()));
		val museum = user.getCurrentMuseum();
		main.startGroup("Музей")
				.record("Заработок", () -> "§b" + MessageUtil.toMoneyFormat(user.getCurrentMuseum().getIncome()))
				.record("Посещений", () -> "§b" + museum.getViews());
		template(main);

		SimpleBoardObjective excavation = IScoreboardService.get().getPlayerObjective(user.getUuid(), "excavation");
		excavation.startGroup("Раскопки")
				.record("Ударов", () -> Math.max(user.getExcavation() != null ? user.getExcavation().getHitsLeft() : 0, 0) + " осталось")
				.record("Уровень", () -> Levels.formatExperience(user.getExperience()));
		template(excavation);

		IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");
	}

}
