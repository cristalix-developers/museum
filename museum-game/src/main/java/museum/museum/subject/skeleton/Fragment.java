package museum.museum.subject.skeleton;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.Map;

import static museum.museum.subject.skeleton.Displayable.orientedOffset;

@Data
public class Fragment implements Displayable {

	private final String address;
	private final Map<Piece, V4> pieceOffsetMap = new Reference2ObjectArrayMap<>();

	@Override
	public void show(Player player, V4 position) {
		pieceOffsetMap.forEach((piece, offset) -> piece.show(player, orientedOffset(position, offset)));
	}

	@Override
	public void update(Player player, V4 position) {
		pieceOffsetMap.forEach((piece, offset) -> piece.update(player, orientedOffset(position, offset)));
	}

	@Override
	public void hide(Player player) {
		for (Piece piece : pieceOffsetMap.keySet())
			piece.hide(player);
	}

}
