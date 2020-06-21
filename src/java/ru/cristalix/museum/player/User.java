package ru.cristalix.museum.player;

import clepto.bukkit.EventPipe;
import clepto.bukkit.PlayerWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.experimental.Delegate;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.cristalix.core.util.UtilNetty;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.excavation.Excavation;
import ru.cristalix.museum.museum.subject.skeleton.Skeleton;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.Coin;
import ru.cristalix.museum.museum.subject.Subject;
import ru.cristalix.museum.util.Levels;
import ru.cristalix.museum.util.MessageUtil;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class User implements PlayerWrapper {

    @Delegate
	private final UserInfo info;

    private Player player;
    private PlayerConnection connection;

    private final Map<String, Museum> museums;
    private final Map<String, Skeleton> skeletons;
    private Subject currentSubject;
    private Museum currentMuseum;
    private Set<Coin> coins;
    private Excavation excavation;

    public User(UserInfo info) {
    	this.info = info;
    	this.museums = this.getMuseumInfos().stream()
				.map(museumInfo -> new Museum(museumInfo, this))
				.collect(Collectors.toMap(Museum::getAddress, m -> m));
    	this.skeletons = this.getSkeletonInfos().stream()
				.map(Skeleton::new)
				.collect(Collectors.toMap(Skeleton::getAddress, s -> s));
	}

	public CraftPlayer getPlayer() {
    	return (CraftPlayer) player;
	}


    public void sendAnime() {
        ByteBuf buffer = Unpooled.buffer();
        UtilNetty.writeVarInt(buffer, excavation == null ? -1 : excavation.getHitsLeft() > 0 ? excavation.getHitsLeft() : -2);
        connection.sendPacket(new PacketPlayOutCustomPayload("museum", new PacketDataSerializer(buffer)));
    }

    public void giveExperience(long exp) {
    	int prevLevel = getLevel();
		info.experience += exp;
		int newLevel = getLevel();
		if (newLevel != prevLevel) {
            MessageUtil.find("levelup")
                    .set("level", newLevel)
                    .set("exp", Levels.getRequiredExperience(newLevel + 1) - getExperience())
                    .send(this);
        }
    }

	public long getExperienceToNextLevel() {
    	return Levels.getRequiredExperience(getLevel() + 1) - getExperience();
	}

	public int getLevel() {
    	return Levels.getLevel(getExperience());
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

	public Skeleton getSkeleton(String address) {
		return skeletons.get(address);
	}

	public void sendPacket(Packet<?> packet) {
    	connection.sendPacket(packet);
	}

}
