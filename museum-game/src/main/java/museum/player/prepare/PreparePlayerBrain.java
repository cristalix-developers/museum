package museum.player.prepare;

import clepto.bukkit.Cycle;
import clepto.bukkit.item.Items;
import clepto.bukkit.world.Label;
import com.destroystokyo.paper.Title;
import implario.ListUtils;
import lombok.Getter;
import lombok.val;
import me.func.mod.Anime;
import museum.App;
import museum.content.DailyRewardManager;
import museum.content.WeekRewards;
import museum.data.PickaxeType;
import museum.fragment.Gem;
import museum.fragment.GemType;
import museum.fragment.Meteorite;
import museum.museum.Museum;
import museum.player.User;
import museum.player.pickaxe.PickaxeUpgrade;
import museum.util.LocationUtil;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.formatting.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static clepto.bukkit.Cycle.exit;

/**
 * @author func 26.08.2020
 * @project museum
 */
public class PreparePlayerBrain implements Prepare {

    public static final Prepare INSTANCE = new PreparePlayerBrain();
    public static final int EXPERIENCE = 3;
    public static final long REWARD_DELAY_HOURS = 18;

    private final List<Label> dots;
    private final List<Title> titles = new ArrayList<>();

    @Getter
    public final static ItemStack hook = Items.render("hook").asBukkitMirror();

    public PreparePlayerBrain() {
        dots = App.getApp().getMap().getLabels("guide");

        // При next след. текст становится на второе место
        Stream.of(
                "Привет! 䀈", "Это.nextТвой. Музей. 㸾", "Заполняйnextвитрины 㜤",
                "Раскапывайnextдинозавров 㿿", "Находи секреты 㜰", "Кастомизируй 㟡",
                "Играй сnextдрузьями 㭿", "Удачи! 㲺"
        ).map(line -> {
            if (line.contains("next")) {
                String[] separated = line.split("next");
                return new Title(separated[0], separated[1]);
            }
            return new Title(line);
        }).forEach(titles::add);

        for (Label label : dots)
            LocationUtil.resetLabelRotation(label, 1);

        dots.sort(Comparator.comparing(dot -> Integer.parseInt(dot.getTag().split("\\s++")[0])));
    }

    @Override
    public void execute(User user, App app) {
        final CraftPlayer player = user.getPlayer();

        user.sendMessage((user.isMessages() ? "Отключить" : "Включить") + " экранные сообщения игроков /con");

        if (player.hasPlayedBefore() || user.getExperience() >= EXPERIENCE) {
            val now = System.currentTimeMillis();
            // Обнулить комбо сбора наград если прошло больше суток или комбо > 7
            if ((user.getDay() > 0 && now - user.getLastTimeRewardClaim() * 10000 > 24 * 60 * 60 * 1000) || user.getDay() > 6) {
                user.setDay(0);
            }
            if (now - user.getLastTimeRewardClaim() * 10000 > REWARD_DELAY_HOURS * 60 * 60 * 10000) {
                user.setLastTimeRewardClaim(now / 10000);
                DailyRewardManager.open(user);

                val dailyReward = WeekRewards.values()[user.getDay()];
                user.getPlayer().sendMessage(Formatting.fine("Ваша ежедневная награда: " + dailyReward.getTitle()));
                dailyReward.getGive().accept(user);
                user.setDay(user.getDay() + 1);

                // Бонус к ежедневной награде
                int reward = 10000;
                if (user.getPrefix() != null && user.getPrefix().equals("㧥"))
                    reward = reward + 20000;
                user.giveMoney(reward);
            } else {
                Anime.topMessage(user.handle(), "Добро пожаловать в ваш §bМузей§f! 㗩");
            }
            return;
        }

        Cycle.run(5 * 20, titles.size(), iteration -> {
            if (!player.isOnline()) {
                exit();
                return;
            }
            if (iteration >= titles.size() - 1) {
                if (user.getExperience() >= EXPERIENCE)
                    player.teleport(dots.get(dots.size() - 1).toCenterLocation());
                user.giveExperience(EXPERIENCE);
                ((Museum) user.getState()).giveMenu(user);
                exit();
                return;
            }
            player.sendTitle(titles.get(iteration));
            player.teleport(dots.get(iteration).toCenterLocation());
        });
    }

    private static String getRare(String string) {
        return string.contains("⭐⭐⭐") ? "LEGENDARY" : string.contains("⭐⭐") ? "EPIC" : "RARE";
    }

    public static ItemStack getPickaxeImage(PickaxeType pickaxeType) {
        val pickaxe = clepto.bukkit.item.Items.render(pickaxeType.name().toLowerCase()).asBukkitMirror();
        val meta = pickaxe.getItemMeta();
        meta.addEnchant(Enchantment.DIG_SPEED, meta.getEnchantLevel(Enchantment.DIG_SPEED), true);
        pickaxe.setItemMeta(meta);
        return pickaxe;
    }

    public static void giveDrop(User owner) {
        val gem = new Gem(ListUtils.random(GemType.values()).name() + ":" + (Math.random() * 1.1) + ":10000");
        gem.setCustomRare(getRare(gem.getType().getTitle()));
        gem.give(owner);
        val meteor = new Meteorite("meteor_" + ListUtils.random(Meteorite.Meteorites.values()).name());
        meteor.setCustomRare(getRare(meteor.getItem().getItemMeta().getDisplayName()));
        meteor.give(owner);

        Anime.openLootBox(owner.handle(), gem, meteor);
    }

    public static void givePickaxe(User user) {
        val pickaxe = Items.render(user.getPickaxeType().name().toLowerCase()).asBukkitMirror();
        val meta = pickaxe.getItemMeta();
        meta.addEnchant(Enchantment.DIG_SPEED, (int) (meta.getEnchantLevel(Enchantment.DIG_SPEED) + PickaxeUpgrade.EFFICIENCY.convert(user)), true);
        pickaxe.setItemMeta(meta);
        user.getInventory().addItem(pickaxe);
    }
}
