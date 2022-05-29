package museum.museum.map;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Location;

/**
 * @author func 05.10.2020
 * @project museum
 */
@Getter
@SuperBuilder
public class StallPrototype extends SubjectPrototype {

	private final Location spawn;

}