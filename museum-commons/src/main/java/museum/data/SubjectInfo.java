package museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.math.D2;
import ru.cristalix.core.math.V3;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SubjectInfo implements Info, Unique {

	public final UUID uuid;
	public final String prototypeAddress;

	public V3 location;
	public D2 rotation;
	public String metadata;
	public int slot;

	private Color color = Color.WHITE;

	public static SubjectInfo generateNew(String prototypeAddress) {
		return new SubjectInfo(UUID.randomUUID(), prototypeAddress);
	}

	public SubjectInfo duplicate() {
		return new SubjectInfo(UUID.randomUUID(), prototypeAddress, location, rotation, metadata, slot, color);
	}

}
