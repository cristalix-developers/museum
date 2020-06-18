package ru.func.museum.player.prepare;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;
import ru.func.museum.App;
import ru.func.museum.player.User;
import ru.func.museum.util.MessageUtil;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareScoreBoard implements Prepare {

    private void template(SimpleBoardObjective objective) {
        objective.setDisplayName("Музей археологии");
        objective.startGroup("Сервер")
                .record("Онлайн", () -> Bukkit.getOnlinePlayers().size() + "")
                .record("Свободно", () -> Runtime.getRuntime().freeMemory()/1024/1024 + "мб")
                .record("TPS", () -> String.format("%.2f", Bukkit.getTPS()[0]));
    }

    @Override
    public void execute(User user, App app) {
        SimpleBoardObjective main = IScoreboardService.get().getPlayerObjective(user.getUuid(), "main");
        main.startGroup("Игрок")
                .record("Уровень", () -> "§b" + user.getGlobalLevel() + " §7" + user.getRequiredExperience(user.getRequiredExperience(0) - user.getExperience()) + "/" + user.getRequiredExperience(0))
                .record("Баланс", () -> "§a" + MessageUtil.toMoneyFormat(user.getMoney()));
        val museum = user.getCurrentMuseum();
        main.startGroup("Музей")
                .record("Заработок", () -> "§b" + MessageUtil.toMoneyFormat(user.getCurrentMuseum().getIncome()))
                .record("Посещений", () -> "§b" + museum.getViews());
        template(main);

        SimpleBoardObjective excavation = IScoreboardService.get().getPlayerObjective(user.getUuid(), "excavation");
        excavation.startGroup("Раскопки")
                .record("Ударов", () -> Math.max(user.getBreakLess(), 0) + " осталось")
                .record("Уровень", () -> "§b" + user.getGlobalLevel() + " §7" + user.getRequiredExperience(user.getRequiredExperience(0) - user.getExperience()) + "/" + user.getRequiredExperience(0));
        template(excavation);

        IScoreboardService.get().setCurrentObjective(user.getUuid(), "main");
    }
}
