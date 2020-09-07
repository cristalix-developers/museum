package museum.museum.subject.skeleton;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class V4 implements Cloneable {

	public static V4 Y = new V4(0, 1, 0, 0);

	public double x;
	public double y;
	public double z;
	public float rot;

	public V4 nullify() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.rot = 0;
		return this;
	}

	public V4 add(double x, double y, double z) {
		return add(x, y, z, 0);
	}

	public V4 add(double x, double y, double z, float yaw) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.rot += rot;
		this.rot %= 360;
		return this;
	}

	public V4 sum(V4 that) {
		if (that == null) return this.clone();
		return new V4(
				this.x + that.x,
				this.y + that.y,
				this.z + that.z,
				this.rot + that.rot
		);
	}

	public V4 posSum(V4 that) {
		if (that == null) return this.clone();
		return new V4(
				this.x + that.x,
				this.y + that.y,
				this.z + that.z,
				this.rot
		);
	}

	public static V4 sum(V4 a, V4 b) {
		return a == null ? b : a.sum(b);
	}

	public V4 rotate(V4 axis, float degrees) {
		if (axis.y == 0 || axis.x != 0 || axis.z != 0)
			// ToDo: Implement quaternion rotation
			throw new UnsupportedOperationException("Non-Y axis rotation is yet to be implemented.");

		double prevX = this.x;
		double prevZ = this.z;
		double radians = Math.toRadians(degrees);
		this.x = prevX * cos(radians) - prevZ * sin(radians);
		this.z = prevX * sin(radians) + prevZ * cos(radians);
		this.rot += degrees;
		return this;
	}

	public Vector toVector() {
		return new Vector(x, y, z);
	}

	public static V4 fromVector(Vector vec) {
		return new V4(vec.x, vec.y, vec.z, 0);
	}

	public Location toLocation(World world) {
		return new Location(world, x, y, z, rot, 0);
	}
	public static V4 fromLocation(Location loc) {
		return new V4(loc.x, loc.y, loc.z, loc.yaw);
	}

	@Override
	public V4 clone() {
		try {
			return (V4) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "[" + s(x) + ", " + s(y) + ", " + s(z) + "] (" + s(rot) + "deg)";
	}

	private static String s(double d) {
		return (int) (d * 10) / 10.0 + "";
	}

}
