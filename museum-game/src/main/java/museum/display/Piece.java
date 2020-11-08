package museum.display;

import lombok.val;

import java.util.HashMap;
import java.util.Map;

public interface Piece {

	static V5 orientedOffset(V5 positionRotation, V5 offset) {
		V5 orientedOffset = offset.clone().rotate(V5.Y, positionRotation.yaw);
		return V5.sum(positionRotation, orientedOffset).setYaw(orientedOffset.yaw);
	}

	default Map<StandDisplayable, V5> transpose(V5 origin) {
		Map<StandDisplayable, V5> map = new HashMap<>();
		recursiveTraverse(map, origin);
		return map;
	}

	default void recursiveTraverse(Map<StandDisplayable, V5> buffer, V5 origin) {
		val map = getChildrenMap();
		if (map == null) {
			buffer.put((StandDisplayable) this, origin);
			return;
		}
		for (val entry : map.entrySet()) {
			Piece piece = entry.getKey();
			V5 pieceOffset = entry.getValue();
			piece.recursiveTraverse(buffer, orientedOffset(origin, pieceOffset));
		}
	}

	default Map<? extends Piece, V5> getChildrenMap() {
		return null;
	}

}
