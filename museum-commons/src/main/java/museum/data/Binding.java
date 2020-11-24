package museum.data;

import lombok.Data;

import java.util.UUID;

@Data
public class Binding {

	private final UUID holderId;
	private final double x;
	private final double y;
	private final double z;
	private final float rotation;

}
