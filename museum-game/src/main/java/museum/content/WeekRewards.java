package museum.content;

import lombok.AllArgsConstructor;
import lombok.Getter;
import museum.fragment.Gem;
import museum.fragment.GemType;
import museum.player.User;
import museum.player.prepare.PreparePlayerBrain;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.item.Items;

import java.util.function.Consumer;

/**
 * @author Рейдж 26.08.2021
 * @project museum
 */

@AllArgsConstructor
@Getter
public enum WeekRewards {
    ONE("§e5000 монет", new ItemStack(Material.GOLD_INGOT), user -> user.giveMoney(5000)),
    TWO("§b100 опыта", new ItemStack(Material.EXP_BOTTLE), user ->
            user.giveExperience(100)),
    THREE(GemType.RUBY.getTitle(),
            new Gem(GemType.RUBY.name() + ':' + 0.8 + ":10000").getItem(), user ->
            new Gem(GemType.RUBY.name() + ':' + 0.8 + ":10000").give(user)),
    FOUR("§b500 опыта", new ItemStack(Material.EXP_BOTTLE), user ->
            user.giveExperience(500)),
    FIVE("§e50000 монет", new ItemStack(Material.DIAMOND), user ->
            user.giveMoney(50000)),
    SIX("§b1 Лутбокс", new ItemStack(Material.CHEST), PreparePlayerBrain::giveDrop),
    SEVEN(GemType.BRILLIANT.getTitle(), new Gem(GemType.BRILLIANT.name() + ':' + 1.0 + ":10000").getItem(),
            user -> new Gem(GemType.BRILLIANT.name() + ':' + 1.0 + ":10000").give(user)
    );

    private String title;
    private ItemStack icon;
    private Consumer<User> give;
}
