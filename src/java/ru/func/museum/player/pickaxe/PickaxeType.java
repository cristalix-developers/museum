package ru.func.museum.player.pickaxe;

import clepto.bukkit.Lemonade;
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
    DEFAULT("любительская", 0, Lemonade.get("pickaxe").render(), new DefaultPickaxe()),
    PROFESSIONAL("профессиональная", 10000, Lemonade.get("pickaxe_professional").render(), new ProfessionalPickaxe()),
    PRESTIGE("престижная", 100000, Lemonade.get("pickaxe_prestige").render(), new PrestigePickaxe());

    private String name;
    private int price;
    private ItemStack item;
    private Pickaxe pickaxe;
}
