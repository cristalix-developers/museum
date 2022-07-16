package museum.worker;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.val;
import me.func.mod.Npc;
import me.func.mod.data.NpcSmart;
import me.func.protocol.npc.NpcBehaviour;
import me.func.protocol.npc.NpcData;
import museum.App;
import museum.cosmos.Cosmos;
import museum.util.StandHelper;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author func 17.07.2020
 * @project museum
 */
@UtilityClass
public class WorkerUtil {

    private final Location cosmos = new Location(App.getApp().getWorld(), 278, 90, -147);
    private final Location ADVERTISING_LOCATION = App.getApp().getMap().getLabel("advertising");
    private final String WEB_DATA = "https://webdata.c7x.dev/textures/skin/";

    public void init(App app) {
        // Формат таблички: .p simplenpc <Имя жителя> </команда>
        app.getMap().getLabels("simplenpc").forEach(label -> {
            ConfigurationSection data = app.getConfig().getConfigurationSection("npc." + label.getTag().split("\\s+")[0]);
            val skin = data.getString("skin");
            val npc = Npc.npc(new NpcData(
                    (int) (Math.random() * Integer.MAX_VALUE),
                    UUID.randomUUID(),
                    label.x + .5,
                    label.y,
                    label.z + .5,
                    1000,
                    data.getString("title"),
                    NpcBehaviour.STARE_AT_PLAYER,
                    label.pitch,
                    label.yaw,
                    skin,
                    skin.substring(skin.length() - 10),
                    true,
                    false,
                    false,
                    false
            ));
            npc.setClick(event -> event.player.performCommand(data.getString("command")));
        });

        new StandHelper(cosmos.clone().add(.5, .4, -.5))
                .customName("§b§lКОСМОС")
                .isInvisible(true)
                .isMarker(true)
                .hasGravity(false)
                .build();

        new StandHelper(cosmos.clone().add(.5, 0, -.5))
                .customName("§eКЛИК НА МЕНЯ")
                .isInvisible(true)
                .isMarker(true)
                .hasGravity(false)
                .slot(EnumItemSlot.HEAD, Cosmos.EARTH)
                .build();
    }

    @Getter
    private final NpcSmart npc = new NpcSmart(new NpcData(
            (int) (Math.random() * Integer.MAX_VALUE),
            UUID.randomUUID(),
            ADVERTISING_LOCATION.x + 0.5,
            ADVERTISING_LOCATION.y,
            ADVERTISING_LOCATION.z + 0.5,
            1000,
            "",
            NpcBehaviour.STARE_AT_PLAYER,
            ADVERTISING_LOCATION.pitch,
            ADVERTISING_LOCATION.yaw,
            "",
            "",
            true,
            false,
            false,
            false
    ),
            null,
            App.getApp().getWorld().getUID(),
            null,
            null,
            null,
            null,
            null,
            null
    );

    public NpcSmart advertisingNpc(Player player) {
        val skin = WEB_DATA + player.getUniqueId();
        npc.getData().setName(player.getName());
        npc.getData().setSkinUrl(skin);
        npc.getData().setSkinDigest(skin.substring(skin.length() - 10));
        npc.setClick(event -> {
            if (player.isOnline())
                event.getPlayer().performCommand("travel " + player.getName())
        });
        Npc.npc(npc.getData());
        return npc;
    }
}