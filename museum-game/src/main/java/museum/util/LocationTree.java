package museum.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@UtilityClass
public class LocationTree {

	public <T> List<T> order(List<T> points, Function<T, Location> converter) {
		List<T> result = new ArrayList<>();
		T currentNode = points.iterator().next();
		points.remove(currentNode);
		result.add(currentNode);
		while (true) {
			Location currentNodeLocation = converter.apply(currentNode);
			double minimalDistance = Double.MAX_VALUE;
			T closest = null;
			for (T variant : points) {
				if (currentNode == variant) continue;
				Location variantLocation = converter.apply(variant);
				double distance = currentNodeLocation.distanceSquared(variantLocation);
				if (distance > minimalDistance) continue;
				minimalDistance = distance;
				closest = variant;
			}
			if (closest == null) return result;
			points.remove(currentNode = closest);
			result.add(currentNode);
		}
	}

}
