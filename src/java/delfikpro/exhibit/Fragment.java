package delfikpro.exhibit;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

@Data
public class Fragment {

	private final String parentAddress;
	private final int id;
	private final List<Piece> pieces;

	public void show(Player player, Location origin) {
		for (Piece piece : pieces)
			piece.show(player, origin);
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
