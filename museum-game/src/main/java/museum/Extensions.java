package museum;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Iterator;

/**
 * @author func 18.10.2020
 * @project museum
 */
public class Extensions {

	public static Vector plus(Vector self, Vector another) {
		return self.clone().add(another);
	}

	public static Location plus(Location self, Vector vector) {
		return self.clone().add(vector);
	}

	public static Location plus(Location self, Iterable<Number> vector) {
		Iterator<Number> iterator = vector.iterator();
		double x = iterator.hasNext() ? iterator.next().doubleValue() : 0;
		double y = iterator.hasNext() ? iterator.next().doubleValue() : 0;
		double z = iterator.hasNext() ? iterator.next().doubleValue() : 0;
		return new Location(self.getWorld(), self.x + x, self.y + y, self.z + z);
	}
}