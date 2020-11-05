package museum.service.player;

import lombok.Data;
import lombok.ToString;
import museum.data.UserInfo;
import ru.cristalix.core.realm.RealmId;

import java.util.UUID;

@Data
public class MuseumPlayer {

	private final UUID uuid;
	private String name;
	private RealmId realmId;

	@ToString.Exclude
	private UserInfo info;

}
