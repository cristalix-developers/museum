package ru.func.museum.player.prepare;

import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

/**
 * @author func 03.06.2020
 * @project Museum
 */
public class PrepareScoreBoard implements Prepare {
    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
         /*SimpleBoardObjective objective = IScoreboardService.get().getPlayerObjective(player.getUniqueId(), "main");

        objective.setDisplayName("Музей архиологии");
        objective.startGroup("Игрок")
                .record("Уровень", () -> archaeologist.getLevel() + "")
                .record("Осталось", () -> archaeologist.expNeed() + " опыта")
                .record("Валюта", () -> archaeologist.getMoney() + "$");
        objective.startGroup("Сервер")
                .record("Онлайн", () -> Bukkit.getOnlinePlayers().size() + "");

        IScoreboardService.get().setCurrentObjective(player.getUniqueId(), "main");*/
    }
}
