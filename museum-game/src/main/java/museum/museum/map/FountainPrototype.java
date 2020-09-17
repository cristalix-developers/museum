package museum.museum.map;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Location;

/**
 * @author func 18.09.2020
 * @project museum
 */
@Getter
@SuperBuilder
public class FountainPrototype extends SubjectPrototype {

	private final Location source;

}
