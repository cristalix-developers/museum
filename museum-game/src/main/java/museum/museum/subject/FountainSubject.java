package museum.museum.subject;

import museum.data.SubjectInfo;
import museum.museum.map.FountainPrototype;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import org.bukkit.Color;
import org.bukkit.Location;

/**
 * @author func 18.09.2020
 * @project museum
 */
public class FountainSubject extends Subject {

	private final Color color;
	private final Location source;

	public FountainSubject(SubjectPrototype prototype, SubjectInfo info, User owner, Color color, Location source) {
		super(prototype, info, owner);
		this.color = color;
		this.source = ((FountainPrototype) prototype).getSource();
	}

	public void throwWater(User user) {
		if (user.getLocation().distanceSquared(prototype.getBox().getCenter()) > 1024)
			return;
		// todo: доделать логику
	}
}
