package museum.museum.subject.skeleton;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import lombok.Data;

import java.util.Map;

@Data
public class Fragment implements Piece {

	private final String address;
	private final Map<AtomPiece, V4> childrenMap = new Reference2ObjectArrayMap<>();

}
