package ru.func.museum.listener;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.func.museum.App;
import ru.func.museum.museum.hall.Hall;

/**
 * @author func 08.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class MoveListener implements Listener {

    private App app;

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        val from = e.getFrom();
        val to = e.getTo();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            val player = e.getPlayer();
            val archaeologist = app.getArchaeologistMap().get(player.getUniqueId());

            if (archaeologist.isOnExcavation())
                return;

            // Если игрок теперь не в выбранном холле, тогда заменить холл
            if (!archaeologist.getCurrentHall().isInside(to)) {
                for (Hall hall : archaeologist.getCurrentMuseum().getHalls()) {
                    if (hall.isInside(to)) {
                        archaeologist.setCurrentHall(hall);
                        break;
                    }
                }
            }

            // Попытка скушать монетки
            archaeologist.getCoins().removeIf(coin -> coin.pickUp(player, archaeologist, to, 1.7));
        }
    }
}
