package ru.cristalix.museum.data.subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.math.D2;
import ru.cristalix.core.math.V3;

@Data
@AllArgsConstructor
public class SubjectInfo implements Cloneable {

	public final String prototypeAddress;

	public V3 location;

	public D2 rotation;
	public String metadata;
	private Color color;

	@Override
	public SubjectInfo clone() {
		try {
			return (SubjectInfo) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

}
