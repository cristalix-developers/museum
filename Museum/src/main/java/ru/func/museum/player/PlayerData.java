package ru.func.museum.player;

import lombok.*;
import ru.func.museum.element.Element;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.museum.AbstractMuseum;
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
    private ExcavationType lastExcavation;
    private boolean onExcavation;
    private List<UUID> friendList;
    private PickaxeType pickaxeType;
    private List<AbstractMuseum> museumList;
    private List<Element> elementList;
}
