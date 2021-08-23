package museum.ticker.detail;

import clepto.bukkit.B;
import implario.ListUtils;
import lombok.val;
import museum.App;
import museum.museum.subject.skeleton.Fragment;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.PlayerDataManager;
import museum.prototype.Managers;
import museum.ticker.Event;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * @author Рейдж 23.08.2021
 * @project museum
 */
public class Auction implements Event {

    private SkeletonPrototype proto;
    private final PlayerDataManager playerDataManager = App.getApp().getPlayerDataManager();

    @Override
    public void tick(int... args) {
        if (args[0] % 3600000 == 0)
            start();
        else if (args[0] % (3600000 + 600000) == 0)
            end();
    }

    @Override
    public void start() {
        playerDataManager.setRateBegun(true);
        proto = ListUtils.random(new ArrayList<>(Managers.skeleton));
        B.bc("Житель нашёл кость " + proto.getTitle() + " участвовать в торгах /rate");
    }

    @Override
    public void end() {
        if (playerDataManager.getMembers().isEmpty()) {
            B.bc("Ни кто не участвовал в аукционе. Вещь осталась у жителя");
            playerDataManager.setRateBegun(false);
            return;
        }

        val maxEntry = Collections.max(playerDataManager.getMembers().entrySet(), Map.Entry.comparingByValue());
        val user = App.getApp().getUser(Bukkit.getPlayer(maxEntry.getKey()));

        Fragment fragment = ListUtils.random(proto.getFragments().toArray(new Fragment[0]));
        Skeleton skeleton = user.getSkeletons().supply(proto);

        user.getPlayer().sendMessage("Вы победили в аукционе и выиграли кость " + proto.getTitle());
        skeleton.getUnlockedFragments().add(fragment);

        playerDataManager.getMembers().clear();
        playerDataManager.setRateBegun(false);
    }
}
