package museum.museum.subject.skeleton;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Displayable {

	void show(Player player, V4 position);

	default void show(Player player, Location location) {
		this.show(player, V4.fromLocation(location));
	}

	void update(Player player, V4 position);

	default void update(Player player, Location location) {
		this.update(player, V4.fromLocation(location));
	}

	void hide(Player player);

	static V4 orientedOffset(V4 positionRotation, V4 offset) {
		V4 orientedOffset = offset.clone().rotate(V4.Y, positionRotation.rot);
		return V4.sum(positionRotation, orientedOffset).setRot(orientedOffset.rot);
	}

}
