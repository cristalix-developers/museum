package ru.func.museum.player;

import lombok.*;
import org.bukkit.entity.Player;
import ru.func.museum.element.Element;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.museum.space.Space;
import ru.func.museum.player.pickaxe.PickaxeType;

import java.util.List;
import java.util.UUID;

/**
 * @author func 23.05.2020
 * @project Museum
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@ToString
public class PlayerData implements Archaeologist {
    private String name;
    private String uuid;
    private long exp;
    private double money;
    private int level;
    private transient int breakLess;
    private ExcavationType lastExcavation;
    private transient Space currentSpace;
    private boolean onExcavation;
    private List<UUID> friendList;
    private PickaxeType pickaxeType;
    private List<AbstractMuseum> museumList;
    private List<Element> elementList;
    private transient AbstractMuseum currentMuseum;
    private int excavationCount;

    @Override
    public void noticeUpgrade(Player player) {
        player.sendMessage("§7[§l§bi§7] Вы достигли §l§b" + level + " §7уровеня! До следующего уровня осталось §l§b" + expNeed() + "§7 опыта.");
    }

    @Override
    public void giveExp(Player player, long exp) {
        this.exp += exp;
        if (expNeed() <= 0) {
            level++;
            noticeUpgrade(player);
        }
    }

    @Override
    public long expNeed() {
        return (long) (Math.pow(level, 2) * 10 - level * 5) * 10 - this.exp;
    }
}
