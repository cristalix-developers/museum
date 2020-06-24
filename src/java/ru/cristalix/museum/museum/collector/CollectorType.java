package ru.cristalix.museum.museum.collector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum CollectorType {
	NONE("отсутствует", new ItemStack(Material.PAPER), 0, 0, 0, -10),
	AMATEUR("любительский", new ItemStack(Material.WORKBENCH), 1, 100_000, 1.5, -1),
	PROFESSIONAL("профессиольный", new ItemStack(Material.WORKBENCH), 2, 400_000, 2, 69),
	PRESTIGE("престижный", new ItemStack(Material.WORKBENCH), 4, 750_000, 3, 99);

	private final String name;
	private final ItemStack head;
	private final int speed;
	private final int cost;
	private final double radius;
	private final int cristalixCost;

}
