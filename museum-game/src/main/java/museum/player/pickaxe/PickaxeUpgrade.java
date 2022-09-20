package museum.player.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import museum.player.User;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum PickaxeUpgrade {
    ADDITIONAL_EXP("§b+0.1XP §fза удар", "§7Вы получаете больше XP §7за удар", "simulators:save_crystal", Material.CLAY_BALL, 10000000, 10, 0.1),
    EXTRA_HITS("§b+3 §fдоп. удара", "§7Вы получаете дополнительные §7удары кирки", "simulators:donate_pickaxe", Material.GOLD_PICKAXE, 500000, 15, 3),
    EFFICIENCY("§b+1 §fэфф. кирки", "§7Вы получаете эффективность кирки §7за каждый уровень", "skyblock:yield", Material.CLAY_BALL, 10000000, 3, 1),
    BONE_DETECTION("§b+1% §fк нахожд. костей", "§7Вы получаете увеличенный шанс §7к обнаружению костей", "museum:bone_item", Material.CLAY_BALL, 250000, 50, .01),
    DETECTION_OF_RELIQUES("§b+1% §fк нахожд. реликвий", "§7Вы получаете увеличенный шанс §7к обнаружению реликвий", "museum:sink", Material.CLAY_BALL, 200000, 50, .01),
    DUPLICATE("§b+0.1% §fдохода за дубликат", "§7Вы получаете больше дохода за §7уже найденные кости", "other:quest_day_booster", Material.CLAY_BALL, 100000, 500, 0.001),
    ;

    private String title;
    private String lore;
    private String nbt;
    private Material icon;
    private int cost;
    private int maxLevel;
    private double step;

    public double convert(User user) {
        return user.getPickaxeImprovements().get(this) * this.step;
    }
}
