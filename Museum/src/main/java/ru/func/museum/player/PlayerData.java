package ru.func.museum.player;

import lombok.*;
import org.bukkit.entity.Player;
import ru.func.museum.element.Element;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.museum.coin.AbstractCoin;
import ru.func.museum.museum.hall.Hall;
import ru.func.museum.museum.hall.template.space.Space;
import ru.func.museum.player.pickaxe.PickaxeType;

import java.util.List;
import java.util.Set;

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
    private boolean onExcavation;
    private PickaxeType pickaxeType;
    private List<AbstractMuseum> museumList;
    private List<Element> elementList;
    private transient Space currentSpace;
    private transient AbstractMuseum currentMuseum;
    private transient Hall currentHall;
    private transient Set<AbstractCoin> coins;
    private int excavationCount;

    @Override
    public void giveExp(Player player, long exp) {
        this.exp += exp;
        if (expNeed(this.exp) <= 0) {
            level++;
            player.sendMessage(String.format(
                    "§7[§l§bi§7] Вы достигли §l§b%d §7уровеня! До следующего уровня осталось §l§b%d§7 опыта.",
                    level,
                    expNeed(this.exp)
            ));
        }
    }

    @Override
    public long expNeed(long haveExp) {
        return (long) (Math.pow(level, 2) * 10 - level * 5) * 10 - haveExp;
    }
}
