package museum.display;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import lombok.Data;

import java.util.Map;

@Data
public class Fragment implements Piece {

	private final String address;
	private final Map<StandDisplayable, V5> childrenMap = new Reference2ObjectArrayMap<>();

}
