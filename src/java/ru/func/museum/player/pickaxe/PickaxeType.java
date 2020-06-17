package ru.func.museum.player.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.item.Items;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum PickaxeType {
    DEFAULT("любительская", 0, Items.builder()
            .displayName("§bКирка")
            .lore("",
                    "§fКлассическая кирка. Идеальна",
                    "§fдля начинающего, ничего лишнего."
            ).type(Material.IRON_PICKAXE)
            .unbreakable(true)
            .build(), new DefaultPickaxe()
    ), PROFESSIONAL("профессиональная", 10000, Items.builder()
            .displayName("§bПрофессиональная кирка")
            .lore("",
                    "§fКирка для настоящего профи.",
                    "§fЛомает от 1 до 5 блоков.",
                    "",
                    "§fЦена: 10'000$"
            ).type(Material.DIAMOND_PICKAXE)
            .unbreakable(true)
            .build(), new ProfessionalPickaxe()
    ), PRESTIGE("престижная", 100000, Items.builder()
            .displayName("§bПрестижная кирка")
            .lore("",
                    "§fСамая престижная и элегантная",
                    "§fкирка для истинного коллекционера.",
                    "§fЛомает 5 блоков. С вероятностью 50%.",
                    "",
                    "§fЦена: 100'000$"
            ).type(Material.GOLD_PICKAXE)
            .enchantment(Enchantment.DIG_SPEED, 1)
            .unbreakable(true)
            .build(), new PrestigePickaxe()
    );

    private String name;
    private int price;
    private ItemStack item;
    private Pickaxe pickaxe;
}
