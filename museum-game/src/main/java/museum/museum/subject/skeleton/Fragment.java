package museum.museum.subject.skeleton;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

@Data
public class Fragment {

	private final String parentAddress;
	private final String address;
	private final List<Piece> pieces;
	private final int[] legacyIds;

	public void show(Player player, Location origin, boolean inBlock) {
		for (Piece piece : pieces)
			piece.show(player, origin, inBlock);
	}

	public void hide(Player player) {
		for (Piece piece : pieces)
			piece.hide(player);
	}

	public void update(Player player, Location newLocation) {
		for (Piece piece : pieces)
			piece.update(player, newLocation);
	}

}
