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
	PROFESSIONAL(new ProfessionalPickaxe(), 3),
	PRESTIGE(new PrestigePickaxe(), 5);

	private final Pickaxe pickaxe;
	private final int experience;

}
