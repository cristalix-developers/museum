package museum.worker;

import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.player.User;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import ru.cristalix.npcs.data.NpcBehaviour;
import ru.cristalix.npcs.server.Npc;
import ru.cristalix.npcs.server.Npcs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author func 17.07.2020
 * @project museum
 */
@UtilityClass
public class WorkerUtil {

    private final static String defaultSkin = App.getApp().getConfig().getString("npc.default.skin");
    public static final Supplier<NpcWorker> STALL_WORKER_TEMPLATE = () -> new NpcWorker(
            new Location(App.getApp().getWorld(), 0, 0, 0),
            defaultSkin,
            "Работница лавки",
            User::getExperience
    );

    public void init(App app) {
        Npcs.init(app);
        // Формат таблички: .p simplenpc <Имя жителя> </команда>
        app.getMap().getLabels("simplenpc").forEach(label -> {
            ConfigurationSection data = app.getConfig().getConfigurationSection("npc." + label.getTag().split("\\s+")[0]);
            val skin = data.getString("skin");
            Npcs.spawn(Npc.builder()
                    .location(label.clone().add(.5, 0, .5))
                    .name(data.getString("title"))
                    .behaviour(NpcBehaviour.STARE_AT_PLAYER)
                    .onClick(user -> user.performCommand(data.getString("command")))
                    .skinUrl(skin)
                    .skinDigest(skin.substring(skin.length() - 10))
                    .build());
        });
    }
}