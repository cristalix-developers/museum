package museum.data.model;

import lombok.*;
import museum.data.Binding;
import ru.cristalix.core.formatting.Color;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SubjectModel implements BindableModel {

	private final UUID uuid;
	private final String address;
	private Binding binding;
	private Color color = Color.BLUE;

	public static SubjectModel generateNew(String prototypeAddress) {
		return new SubjectModel(UUID.randomUUID(), prototypeAddress);
	}

	public SubjectModel duplicate() {
		return new SubjectModel(UUID.randomUUID(), address, binding, color);
	}

}
