package museum.player.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import museum.player.User;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum PickaxeUpgrade {
    ADDITIONAL_EXP("§b+0.1XP §7за удар", "Вы получаете больше XP\n§7за удар", "simulators:save_crystal", Material.CLAY_BALL, 1000000, 20, 0.05),
    EXTRA_HITS("§b+3 §7дополнительных ударов", "Вы получаете дополнительные\n§7удары кирки", "simulators:donate_pickaxe", Material.GOLD_PICKAXE, 50000, 15, 3),
    EFFICIENCY("§b+1 §7эффективность кирки", "Вы получаете эффективность кирки\n§7за каждый уровень", "skyblock:yield", Material.CLAY_BALL, 1000000, 3, 1),
    BONE_DETECTION("§a+1% §7к обнаружению костей", "Вы получаете увеличенный шанс\n§7к обнаружению костей", "museum:bone_item", Material.CLAY_BALL, 25000, 50, .01),
    DETECTION_OF_RELIQUES("§a+1% §7к обнаружению реликвий", "Вы получаете увеличенный шанс\n§7к обнаружению реликвий", "museum:sink", Material.CLAY_BALL, 20000, 50, .01),
    DUPLICATE("§a+0.1% §7дохода за дубликат костей", "Вы получаете больше дохода за\n§7уже найденные кости", "other:quest_day_booster", Material.CLAY_BALL, 10000, 500, 0.001),
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
