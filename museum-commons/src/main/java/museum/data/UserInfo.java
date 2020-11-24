package museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import museum.data.model.Model;
import ru.cristalix.core.math.V3;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserInfo implements Unique {

	public final UUID uuid;

	public String prefix;
	private int rank;
	private boolean darkTheme;

	public long experience;
	private double money;
	private double income;
	private long timePlayed;
	private long pickedCoinsCount;
	private int excavationCount;

	private V3 lastPosition;

	private final List<Model> models;
	private List<BoosterInfo> localBoosters;
	private List<String> claimedPlaces;

}
