package ru.cristalix.museum.data.subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.math.D2;
import ru.cristalix.core.math.V3;

@Data
@AllArgsConstructor
public class SubjectInfo {

	public final String prototypeAddress;

	/**
	 * Позиция этого субъекта относительно центра музея
	 */
	public V3 locationDelta;

	public D2 rotation;

	private Color color;

	public String metadata;

}
