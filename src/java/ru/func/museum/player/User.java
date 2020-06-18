package ru.func.museum.player;

import clepto.bukkit.EventPipe;
import clepto.bukkit.PlayerWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.experimental.Delegate;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.cristalix.core.util.UtilNetty;
import ru.func.museum.data.UserInfo;
import ru.func.museum.element.Element;
import ru.func.museum.museum.Museum;
import ru.func.museum.museum.coin.Coin;
import ru.func.museum.museum.hall.Hall;
import ru.func.museum.museum.hall.template.space.Space;
import ru.func.museum.util.MessageUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class User implements PlayerWrapper {

    @Delegate
	private final UserInfo info;

    private Player player;
    private PlayerConnection connection;

    private int breakLess;
    private boolean onExcavation;
    private List<Museum> museums;
    private List<Element> elementList;
    private Space currentSpace;
    private Museum currentMuseum;
    private Hall currentHall;
    private Set<Coin> coins;

    public User(UserInfo info) {
    	this.info = info;
    	this.museums = this.getMuseumInfos().stream()
				.map(museumInfo -> new Museum(museumInfo, this))
				.collect(Collectors.toList());
	}

	public CraftPlayer getPlayer() {
    	return (CraftPlayer) player;
	}


    public void sendAnime() {
        ByteBuf buffer = Unpooled.buffer();
        UtilNetty.writeVarInt(buffer, breakLess);
        connection.sendPacket(new PacketPlayOutCustomPayload("museum", new PacketDataSerializer(buffer)));
    }

    public void giveExperience(long exp) {
    	int prevLevel = getGlobalLevel();
		info.experience += exp;
		int newLevel = getGlobalLevel();
		if (newLevel != prevLevel) {
            MessageUtil.find("levelup")
                    .set("level", newLevel)
                    .set("exp", getRequiredExperience(this.getExperience()))
                    .send(this);
        }
    }

    public long getRequiredExperience(long level) {
        return 100 * level * level - 50 * level - getExperience();
    }

    public int getGlobalLevel() {
    	return (int) (0.25 + 0.05 * Math.sqrt(4 * getExperience() + 25));
	}

	public UserInfo generateUserInfo() {

		return info;
	}

	@Override
	public Location getSpawnLocation() {
		return null;
	}

	@Override
	public EventPipe<?> getEventPipe() {
		return null;
	}

	@Override
	public void setup() {

	}

}
