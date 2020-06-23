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
	DEFAULT(new DefaultPickaxe(), 5),
	PROFESSIONAL(new ProfessionalPickaxe(), 20),
	PRESTIGE(new PrestigePickaxe(), 50);

	private final Pickaxe pickaxe;
	private final int experience;

}
