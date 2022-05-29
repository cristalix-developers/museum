package museum.worker;

import lombok.experimental.UtilityClass;
import lombok.val;
import me.func.mod.Npc;
import me.func.protocol.npc.NpcBehaviour;
import me.func.protocol.npc.NpcData;
import museum.App;
import museum.cosmos.Cosmos;
import museum.util.StandHelper;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

/**
 * @author func 17.07.2020
 * @project museum
 */
@UtilityClass
public class WorkerUtil {

    private final Location cosmos = new Location(App.getApp().getWorld(), 278, 90, -147);

    public final static String defaultSkin = App.getApp().getConfig().getString("npc.default.skin");

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
}