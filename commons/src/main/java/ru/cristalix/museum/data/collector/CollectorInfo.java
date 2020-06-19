package ru.cristalix.museum.data.collector;

import lombok.Data;
import ru.cristalix.core.math.V3;

import java.util.List;

@Data
public class CollectorInfo {

	private final int id;
	private final List<V3> customRoute;
	private final CollectorType type;

}
