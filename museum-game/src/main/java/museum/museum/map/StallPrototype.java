package museum.museum.map;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import museum.worker.NpcWorker;
import org.bukkit.Location;

import java.util.function.Supplier;

/**
 * @author func 05.10.2020
 * @project museum
 */
@Getter
@SuperBuilder
public class StallPrototype extends SubjectPrototype {

	private final Supplier<NpcWorker> worker;
	private final Location spawn;

}