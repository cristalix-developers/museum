package ru.func.museum.player.prepare;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.cristalix.core.scoreboard.IScoreboardService;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

import java.text.DecimalFormat;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareScoreBoard implements Prepare {

    private static DecimalFormat numberFormat = new DecimalFormat("###,###,###,###,###,###.##$");

    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        SimpleBoardObjective main = IScoreboardService.get().getPlayerObjective(player.getUniqueId(), "main");
        main.startGroup("Игрок")
                .record("Уровень", () -> "§b" + archaeologist.getLevel() + " §7" + archaeologist.expNeed(archaeologist.expNeed(0) - archaeologist.getExp()) + "/" + archaeologist.expNeed(0))
                .record("Баланс", () -> "§a" + numberFormat.format(archaeologist.getMoney()));
        val museum = archaeologist.getCurrentMuseum();
        main.startGroup("Музей")
                .record("Заработок", () -> "§b" + numberFormat.format(archaeologist.getCurrentMuseum().getSummaryIncrease()))
                .record("Посещений", () -> museum.getViews() + "");
        template(main);

        SimpleBoardObjective excavation = IScoreboardService.get().getPlayerObjective(player.getUniqueId(), "excavation");
        excavation.startGroup("Раскопки")
                .record("Ударов", () -> Math.max(archaeologist.getBreakLess(), 0) + " осталось")
                .record("Уровень", () -> "§b" + archaeologist.getLevel() + " §7" + archaeologist.expNeed(archaeologist.expNeed(0) - archaeologist.getExp()) + "/" + archaeologist.expNeed(0));
        template(excavation);

        IScoreboardService.get().setCurrentObjective(player.getUniqueId(), "main");
    }

    private void template(SimpleBoardObjective objective) {
        objective.setDisplayName("Музей археологии");
        objective.startGroup("Сервер")
                .record("Онлайн", () -> Bukkit.getOnlinePlayers().size() + "")
                .record("TPS", () -> String.format("%.2f", Bukkit.getTPS()[0]));
    }
}
