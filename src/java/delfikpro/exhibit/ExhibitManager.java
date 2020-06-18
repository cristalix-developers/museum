package delfikpro.exhibit;

import clepto.cristalix.WorldMeta;
import ru.cristalix.core.build.models.Point;

import java.util.HashMap;
import java.util.Map;

public class ExhibitManager {

	private final WorldMeta worldMeta;
	private final Map<String, Exhibit> exhibitMap = new HashMap<>();

	public ExhibitManager(WorldMeta worldMeta) {
		this.worldMeta = worldMeta;
		for (Point point : worldMeta.getPoints("model")) {
			int size = parseInt(getNearConfig(point, "size"));
			int pieces = parseInt(getNearConfig(point, "pieces"));
			String name = getNearConfig(point, "name");
			String address = point.getTag();
			exhibitMap.put(address, new Exhibit(name, pieces, address, worldMeta));
		}
	}

	private String getNearConfig(Point root, String criteria) {
		return worldMeta.getPoints(criteria).stream()
				.filter(p -> root.getV3().distanceSquared(p.getV3()) < 9)
				.findFirst()
				.map(Point::getTag)
				.orElse(null);
	}

	public static int parseInt(String string) {
		if (string == null) return 0;
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public Exhibit getExhibit(String address) {
		return exhibitMap.get(address);
	}

}
