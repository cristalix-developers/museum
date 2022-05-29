package museum.cosmos.boer;

import com.google.common.collect.Sets;
import lombok.val;
import museum.App;
import museum.boosters.BoosterType;
import museum.ticker.Ticked;
import org.bukkit.Bukkit;

import java.util.Set;

public class BoerManager implements Ticked {

    private static final Set<Boer> boers = Sets.newConcurrentHashSet();

    @Override
    public void tick(int... args) {
        if (args[0] % 20 == 0) {
            val booster = App.getApp().getPlayerDataManager().calcGlobalMultiplier(BoosterType.BOER);
            val bigBooster = App.getApp().getPlayerDataManager().calcGlobalMultiplier(BoosterType.BIG_BOER);

            val finalBooster = 1 + (booster > 1 ? booster - 1 : 0) + (bigBooster > 1 ? bigBooster - 1 : 0);

            boers.forEach(boer -> {
                val player = Bukkit.getPlayer(boer.getOwner());
                if ((player == null || !player.isOnline() || boer.getSecondsLeft() <= 0) && !boer.getStands().isEmpty()) {
                    boers.remove(boer.boerRemove());
                } else {
                    if (player == null)
                        return;
                    val user = App.getApp().getUser(player);
                    boer.setSecondsLeft(boer.getSecondsLeft() - 1);
                    val seconds = boer.getSecondsLeft();
                    boer.getHead().setCustomName(player.getName() + " §l" + seconds / 60 / 60 % 24 + ":" + seconds / 60 % 60 + ":" + seconds % 60);
                    if ((int) (args[0] / 20.0D) % (boer.getType().getSpeed() / finalBooster) == 0) {
                        user.giveExperience(1.0D, boer.isNotification());
                        user.giveCosmoCrystal(1, boer.isNotification());
                    }
                }
            });
        }
    }

    public static void createActiveBoer(Boer boer) {
        boers.add(boer);
    }
}
