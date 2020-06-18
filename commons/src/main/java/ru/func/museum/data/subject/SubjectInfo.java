package ru.func.museum.data.subject;

import lombok.Data;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.math.D2;
import ru.cristalix.core.math.V3;

@Data
public class SubjectInfo {

	/**
	 * Позиция этого субъекта относительно центра музея
	 */
	public V3 locationDelta;

	public D2 rotation;

	private Color color;

	public String prototype;
	public String metadata;


}
