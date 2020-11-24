package museum.museum.subject;

import lombok.Getter;
import museum.App;
import museum.data.model.SubjectModel;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import org.bukkit.Location;
import ru.cristalix.core.util.UtilV3;

public class MarkerSubject extends Subject {

	private final Location location;

	@Getter
	private final int collectorId;

	public MarkerSubject(SubjectPrototype prototype, SubjectModel info, User user) {
		super(prototype, info, user);
		// ToDo: Fix markers
		this.location = info.getLocation() == null ? null : UtilV3.toLocation(info.getLocation().clone().add(0.5, 0, 0.5), App.getApp().getWorld());
		this.collectorId = info.getMetadata() == null ? 0 : Integer.parseInt(info.getMetadata());
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public void updateInfo() {
		cachedInfo.metadata = String.valueOf(collectorId);
	}

}
