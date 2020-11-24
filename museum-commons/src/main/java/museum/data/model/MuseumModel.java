package museum.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import museum.data.Binding;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class MuseumModel implements Model {

	private final UUID uuid;
	private final String address;
	private final Date creationDate;
	private String title;
	public long views;

}
