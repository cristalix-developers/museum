package ru.func.museum.player;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.entity.Player;
import ru.cristalix.core.util.UtilNetty;
import ru.func.museum.element.Element;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.museum.coin.AbstractCoin;
import ru.func.museum.museum.hall.Hall;
import ru.func.museum.museum.hall.template.space.Space;
import ru.func.museum.player.pickaxe.PickaxeType;
import ru.func.museum.util.MessageUtil;

import java.util.List;
import java.util.Set;

/**
 * @author func 23.05.2020
 * @project Museum
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private transient PlayerConnection connection;
    private int excavationCount;
    private long pickedCoinsCount;

    @Override
    public void incPickedCoinsCount() {
        pickedCoinsCount++;
    }

    @Override
    public void sendAnime() {
        ByteBuf buffer = Unpooled.buffer();
        UtilNetty.writeVarInt(buffer, breakLess);
        connection.sendPacket(new PacketPlayOutCustomPayload("museum", new PacketDataSerializer(buffer)));
    }

    @Override
    public void giveExp(Player player, long exp) {
        this.exp += exp;
        if (expNeed(this.exp) <= 0) {
            level++;
            MessageUtil.find("levelup")
                    .set("level", level)
                    .set("exp", expNeed(this.exp))
                    .send(player);
        }
    }

    @Override
    public long expNeed(long haveExp) {
        return (long) (Math.pow(level, 2) * 10 - level * 5) * 10 - haveExp;
    }
}
