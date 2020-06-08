package ru.func.museum.listener;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.func.museum.App;
import ru.func.museum.museum.coin.AbstractCoin;
import ru.func.museum.museum.coin.Coin;
import ru.func.museum.museum.hall.Hall;
import ru.func.museum.player.pickaxe.Pickaxe;

/**
 * @author func 08.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class MoveListener implements Listener {

    private App app;

    @EventHandler(priority = EventPriority.HIGH)
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


            val connection = ((CraftPlayer) player).getHandle().playerConnection;

            // Попытка скушать монетки
            archaeologist.getCoins().removeIf(coin -> coin.pickUp(connection, archaeologist, to, 1.7));

            // Test
            AbstractCoin coin = new Coin(to.clone().subtract(
                    Pickaxe.RANDOM.nextInt(5) - 2.5,
                    0,
                    Pickaxe.RANDOM.nextInt(5) - 2.5)
            );

            coin.create(connection);
            archaeologist.getCoins().add(coin);
        }
    }
}
