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

    @Override
    public void execute(Player player, User archaeologist, App app) {
        SimpleBoardObjective main = IScoreboardService.get().getPlayerObjective(player.getUniqueId(), "main");
        main.startGroup("Игрок")
                .record("Уровень", () -> "§b" + archaeologist.getLevel() + " §7" + archaeologist.getRequiredExperience(archaeologist.getRequiredExperience(0) - archaeologist.getExp()) + "/" + archaeologist.getRequiredExperience(0))
                .record("Баланс", () -> "§a" + MessageUtil.toMoneyFormat(archaeologist.getMoney()));
        val museum = archaeologist.getCurrentMuseum();
        main.startGroup("Музей")
                .record("Заработок", () -> "§b" + MessageUtil.toMoneyFormat(archaeologist.getCurrentMuseum().getSummaryIncrease()))
                .record("Посещений", () -> "§b" + museum.getViews());
        template(main);

        SimpleBoardObjective excavation = IScoreboardService.get().getPlayerObjective(player.getUniqueId(), "excavation");
        excavation.startGroup("Раскопки")
                .record("Ударов", () -> Math.max(archaeologist.getBreakLess(), 0) + " осталось")
                .record("Уровень", () -> "§b" + archaeologist.getLevel() + " §7" + archaeologist.getRequiredExperience(archaeologist.getRequiredExperience(0) - archaeologist.getExp()) + "/" + archaeologist.getRequiredExperience(0));
        template(excavation);

        IScoreboardService.get().setCurrentObjective(player.getUniqueId(), "main");
    }

    private void template(SimpleBoardObjective objective) {
        objective.setDisplayName("Музей археологии");
        objective.startGroup("Сервер")
                .record("Онлайн", () -> Bukkit.getOnlinePlayers().size() + "")
                .record("Свободно", () -> Runtime.getRuntime().freeMemory()/1024/1024 + "мб")
                .record("TPS", () -> String.format("%.2f", Bukkit.getTPS()[0]));
    }
}
