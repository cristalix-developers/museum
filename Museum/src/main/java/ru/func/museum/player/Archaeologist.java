package ru.func.museum.player;

import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
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

@BsonDiscriminator
public interface Archaeologist {
    String getUuid();

    Set<AbstractCoin> getCoins();

    void setCoins(Set<AbstractCoin> coins);

    void setCurrentHall(Hall hall);

    Hall getCurrentHall();

    AbstractMuseum getCurrentMuseum();

    void setCurrentMuseum(AbstractMuseum currentMuseum);

    PlayerConnection getConnection();

    void setConnection(PlayerConnection connection);

    long getPickedCoinsCount();

    void incPickedCoinsCount();

    Space getCurrentSpace();

    void setCurrentSpace(Space space);

    String getName();

    int getBreakLess();

    void setBreakLess(int breakLess);

    ExcavationType getLastExcavation();

    void setLastExcavation(ExcavationType excavation);

    int getExcavationCount();

    void setExcavationCount(int excavationCount);

    long getExp();
    
    void giveExp(Player player, long exp);
    
    long expNeed(long haveExp);

    double getMoney();

    void setMoney(double money);

    int getLevel();

    boolean isOnExcavation();

    void setOnExcavation(boolean onExcavation);

    PickaxeType getPickaxeType();

    void setPickaxeType(PickaxeType pickaxeType);

    List<AbstractMuseum> getMuseumList();

    List<Element> getElementList();
}
