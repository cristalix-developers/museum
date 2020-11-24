package museum.data.model;

import lombok.Data;

import java.util.UUID;

@Data
public class PickaxeModel implements Model {

	private final UUID uuid;
	private String address;
	private long blocksBroken;
	private String title;

}
