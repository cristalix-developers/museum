package ru.func.museum.player.prepare;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareScoreBoard implements Prepare {

    private String title = "Музей археологии";

    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        SimpleBoardObjective main = IScoreboardService.get().getPlayerObjective(player.getUniqueId(), "main");
        main.startGroup("Игрок")
                .record("Уровень", () -> archaeologist.getLevel() + "")
                .record("Осталось", () -> archaeologist.expNeed() + " опыта")
                .record("Денег", () -> String.format("%.2f$", archaeologist.getMoney()));
        val museum = archaeologist.getCurrentMuseum();
        main.startGroup("Музей")
                .record("Заработок", () -> String.format("%.2f$/сек", museum.getSummaryIncrease()))
                .record("Посещений", () -> museum.getViews() + "");
        template(main);

        SimpleBoardObjective excavation = IScoreboardService.get().getPlayerObjective(player.getUniqueId(), "excavation");
        excavation.startGroup("Раскопки")
                .record("Ударов", () -> Math.max(archaeologist.getBreakLess(), 0) + " осталось")
                .record("Опыта", () -> archaeologist.expNeed() + " осталось");
        template(excavation);

        IScoreboardService.get().setCurrentObjective(player.getUniqueId(), "main");
    }

    private void template(SimpleBoardObjective objective) {
        objective.setDisplayName(title);
        objective.startGroup("Сервер")
                .record("Онлайн", () -> Bukkit.getOnlinePlayers().size() + "");
    }
}
