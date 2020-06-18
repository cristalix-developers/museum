package ru.func.museum.data.collector;

import lombok.Data;
import ru.cristalix.core.math.V3;

import java.util.List;

@Data
public class CollectorInfo {

	private final List<V3> endpoints;
	private final CollectorType type;
	private final String name;

}
