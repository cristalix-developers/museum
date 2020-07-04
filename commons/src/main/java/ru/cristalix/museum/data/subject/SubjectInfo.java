package ru.cristalix.museum.data.subject;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.math.D2;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.data.Info;

@Data
@AllArgsConstructor
public class SubjectInfo implements Cloneable, Info {

	public final String prototypeAddress;

	public V3 location;
	public D2 rotation;
	public String metadata;
	public int slot;

	private Color color;

	public SubjectInfo(String prototypeAddress) {
	    this.prototypeAddress = prototypeAddress;
	    this.color = Color.CYAN;
    }

	@Override
	public SubjectInfo clone() {
		try {
			return (SubjectInfo) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

}
