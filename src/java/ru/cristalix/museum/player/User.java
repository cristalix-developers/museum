package ru.cristalix.museum.player;

import clepto.bukkit.PlayerWrapper;
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
import ru.cristalix.museum.boosters.BoosterType;
import ru.cristalix.museum.data.MuseumInfo;
import ru.cristalix.museum.data.SkeletonInfo;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.excavation.Excavation;
import ru.cristalix.museum.gallery.Warp;
import ru.cristalix.museum.museum.Coin;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.map.MuseumPrototype;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.subject.Subject;
import ru.cristalix.museum.museum.subject.skeleton.Skeleton;
import ru.cristalix.museum.museum.subject.skeleton.SkeletonPrototype;
import ru.cristalix.museum.prototype.Managers;
import ru.cristalix.museum.prototype.Registry;
import ru.cristalix.museum.util.LevelSystem;
import ru.cristalix.museum.util.MessageUtil;

import java.util.Set;

@Data
public class User implements PlayerWrapper {

    @Delegate
    private final UserInfo info;
    private final Registry<MuseumInfo, MuseumPrototype, Museum> museums = new Registry<>(this, Managers.museum, MuseumInfo::new, Museum::new);
    private final Registry<SkeletonInfo, SkeletonPrototype, Skeleton> skeletons = new Registry<>(this, Managers.skeleton, SkeletonInfo::new, Skeleton::new);
    private final Registry<SubjectInfo, SubjectPrototype, Subject> subjects = new Registry<>(this, Managers.subject, SubjectInfo::new, SubjectPrototype::provide);

    private CraftPlayer player;
    private PlayerConnection connection;
    private Warp lastWarp;
    private Museum currentMuseum;
    private Set<Coin> coins;
    private Excavation excavation;

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
        info.getLocalBoosters().removeIf(Booster::hadExpire);

        double sum = 1;
        for (Booster booster : info.getLocalBoosters()) {
            if (booster.getType() == type) {
                sum += booster.getMultiplier() - 1;
            }
        }
        return sum;
    }

}
