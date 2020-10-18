package museum.util;

import clepto.bukkit.world.Label;
import clepto.bukkit.world.Orientation;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@UtilityClass
public class LocationUtil {

	public <T> List<T> orderTree(List<T> points, Function<T, Location> converter) {
		List<T> result = new ArrayList<>();
		if (points.isEmpty()) return null;
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

	public <L extends Label> L resetLabelRotation(L input, int characterOffset) {
		String[] ss = input.getTag().split("\\s++");

		input.setYaw(Integer.parseInt(ss[characterOffset]));
		input.setPitch(Integer.parseInt(ss[++characterOffset]));

		return input;
	}

	public Orientation getOrientation(Location location) {
		float yaw = location.getYaw() % 360;
		if (yaw < 0) yaw += 360;
		if (yaw >= -45 && yaw <= 45) return Orientation.PX;
		if (yaw >= 45 && yaw <= 135) return Orientation.PY;
		if (yaw >= 135 && yaw <= 225) return Orientation.MX;
		return Orientation.MY;
	}

}
