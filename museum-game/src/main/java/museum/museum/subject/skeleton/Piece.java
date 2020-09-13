package museum.museum.subject.skeleton;

import lombok.val;

import java.util.HashMap;
import java.util.Map;

public interface Piece {

	static V4 orientedOffset(V4 positionRotation, V4 offset) {
		V4 orientedOffset = offset.clone().rotate(V4.Y, positionRotation.rot);
		return V4.sum(positionRotation, orientedOffset).setRot(orientedOffset.rot);
	}

	default Map<AtomPiece, V4> transpose(V4 origin) {
		Map<AtomPiece, V4> map = new HashMap<>();
		recursiveTraverse(map, origin);
		return map;
	}

	default void recursiveTraverse(Map<AtomPiece, V4> buffer, V4 origin) {
		val map = getChildrenMap();
		if (map == null) {
			buffer.put((AtomPiece) this, origin);
			return;
		}
		for (val entry : map.entrySet()) {
			Piece piece = entry.getKey();
			V4 pieceOffset = entry.getValue();
			piece.recursiveTraverse(buffer, orientedOffset(origin, pieceOffset));
		}
	}

	default Map<? extends Piece, V4> getChildrenMap() {
		return null;
	}

}
