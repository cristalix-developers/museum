package museum.museum.map;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import museum.worker.NpcWorker;

/**
 * @author func 05.10.2020
 * @project museum
 */
@Getter
@SuperBuilder
public class StallPrototype extends SubjectPrototype {

	private final NpcWorker worker;

}