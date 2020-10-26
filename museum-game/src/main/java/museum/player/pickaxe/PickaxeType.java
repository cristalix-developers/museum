package museum.player.pickaxe;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public enum PickaxeType {

	DEFAULT(new DefaultPickaxe(), 1),
	PROFESSIONAL(new ProfessionalPickaxe(), 1),
	PRESTIGE(new PrestigePickaxe(), 1),
	LEGENDARY(new PrestigePickaxe(), 2);

	private final Pickaxe pickaxe;
	private final int experience;

}
