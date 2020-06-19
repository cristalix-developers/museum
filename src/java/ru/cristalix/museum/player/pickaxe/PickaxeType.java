package ru.cristalix.museum.player.pickaxe;

import clepto.bukkit.Lemonade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum PickaxeType {
    DEFAULT(new DefaultPickaxe()),
    PROFESSIONAL(new ProfessionalPickaxe()),
    PRESTIGE(new PrestigePickaxe());

    private final Pickaxe pickaxe;

    public ItemStack getItem() {
    	return Lemonade.get("pickaxe-" + name().toLowerCase()).render();
	}
}
