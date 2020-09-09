package museum.player;

import clepto.bukkit.event.PlayerWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.experimental.Delegate;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import ru.cristalix.core.util.UtilNetty;
import museum.boosters.BoosterType;
import museum.data.*;
import museum.excavation.Excavation;
import museum.museum.Coin;
import museum.museum.Museum;
import museum.museum.map.MuseumPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.prototype.Managers;
import museum.prototype.Registry;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import museum.util.warp.Warp;

import java.util.HashSet;
import java.util.Set;

@Data
public class User implements PlayerWrapper {

	@Delegate
	private final UserInfo info;
	private final Registry<MuseumInfo, MuseumPrototype, Museum> museums
			= new Registry<>(this, Managers.museum, MuseumInfo::new, Museum::new);
	private final Registry<SkeletonInfo, SkeletonPrototype, Skeleton> skeletons
			= new Registry<>(this, Managers.skeleton, SkeletonInfo::new, Skeleton::new);
	private final Registry<SubjectInfo, SubjectPrototype, Subject> subjects
			= new Registry<>(this, Managers.subject, SubjectInfo::generateNew, SubjectPrototype::provide);

	private CraftPlayer player;
	private PlayerConnection connection;
	private Warp lastWarp;
	private Museum currentMuseum;
	private Excavation excavation;
	private Set<Coin> coins = new HashSet<>();

	public User(UserInfo info) {
		this.info = info;

		this.subjects.addAll(info.getSubjectInfos());
		this.museums.addAll(info.getMuseumInfos());
		this.skeletons.addAll(info.getSkeletonInfos());
	}

	public void sendAnime() {
		ByteBuf buffer = Unpooled.buffer();
		UtilNetty.writeVarInt(buffer, excavation == null ? -2 : excavation.getHitsLeft() > 0 ? excavation.getHitsLeft() : -1);
		connection.sendPacket(new PacketPlayOutCustomPayload("museum", new PacketDataSerializer(buffer)));
	}

	public void giveExperience(long exp) {
		int prevLevel = getLevel();
		info.experience += exp;
		int newLevel = getLevel();
		if (newLevel != prevLevel)
			MessageUtil.find("levelup")
					.set("level", newLevel)
					.set("exp", LevelSystem.getRequiredExperience(newLevel) - getExperience())
					.send(this);
	}

	public int getLevel() {
		return LevelSystem.getLevel(getExperience());
	}

	public UserInfo generateUserInfo() {
		info.museumInfos = museums.getData();
		info.skeletonInfos = skeletons.getData();
		info.subjectInfos = subjects.getData();
		return info;
	}

	public void sendPacket(Packet<?> packet) {
		connection.sendPacket(packet);
	}

	public double calcMultiplier(BoosterType type) {
		info.getLocalBoosters().removeIf(BoosterInfo::hadExpire);

		double sum = 1;
		for (BoosterInfo booster : info.getLocalBoosters()) {
			if (booster.getType() == type) {
				sum += booster.getMultiplier() - 1;
			}
		}
		return sum;
	}

	public void sendPayload(String channel, String payload) {
		ByteBuf buffer = Unpooled.buffer();
		UtilNetty.writeString(buffer, payload);
		PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(channel, new PacketDataSerializer(buffer));
		player.getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public String toString() {
		return this.getDisplayName();
	}

}
