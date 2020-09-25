package museum.player;

import clepto.bukkit.event.PlayerWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.experimental.Delegate;
import museum.App;
import museum.boosters.BoosterType;
import museum.data.*;
import museum.museum.Coin;
import museum.museum.Museum;
import museum.museum.map.MuseumPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.prepare.PrepareScoreBoard;
import museum.prototype.Managers;
import museum.prototype.Registry;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.spigotmc.AsyncCatcher;
import ru.cristalix.core.util.UtilNetty;
import ru.cristalix.core.util.UtilV3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
	private Location lastLocation;
	private State state;
	private long enterTime;

	public User(UserInfo info) {
		this.enterTime = System.currentTimeMillis();
		this.info = info;

		this.skeletons.importInfos(info.getSkeletonInfos());
		this.subjects.importInfos(info.getSubjectInfos());
		this.museums.importInfos(info.getMuseumInfos());

		if (info.getLastPosition() != null)
			this.lastLocation = UtilV3.toLocation(info.getLastPosition(), App.getApp().getWorld());
		else
			this.lastLocation = Managers.museum.getPrototype("main").getSpawn();

		this.state = this.museums.get(Managers.museum.getPrototype("main"));
	}

	public void setState(State state) {
		AsyncCatcher.catchOp("user state change");
		if (this.state != null && this.state != state) this.state.leaveState(this);
		(this.state = state).enterState(this);
		PrepareScoreBoard.setupScoreboard(this);
	}

	public Subject getSubject(UUID uuid) {
		for (Subject subject : this.subjects) {
			if (subject.getCachedInfo().getUuid().equals(uuid)) return subject;
		}
		return null;
	}

	public void sendAnime() {
		ByteBuf buffer = Unpooled.buffer();
		// ToDo: Вернуть счётчик на раскопках!
//		UtilNetty.writeVarInt(buffer, interactItems == null ? -2 : interactItems.getHitsLeft() > 0 ? interactItems.getHitsLeft() : -1);
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

	public Museum getLastMuseum() {
		for (Museum museum : museums) {
			if (museum.getPrototype().getBox().contains(lastLocation))
				return museum;
		}
		return null;
	}

}
