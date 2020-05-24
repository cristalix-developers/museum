package ru.func.museum.player;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import ru.func.museum.element.Element;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.player.pickaxe.PickaxeType;

import java.util.List;
import java.util.UUID;

@BsonDiscriminator
public interface Archaeologist {
    String getUuid();

    String getName();

    double getMoney();

    void setMoney(double money);

    int getLevel();

    void setLevel(int level);

    boolean isOnExcavation();

    void setOnExcavation(boolean onExcavation);

    List<UUID> getFriendList();

    PickaxeType getPickaxeType();

    void setPickaxeType(PickaxeType pickaxeType);

    List<AbstractMuseum> getMuseumList();

    List<Element> getElementList();
}
