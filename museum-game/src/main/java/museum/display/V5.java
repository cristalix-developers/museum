package museum.display;

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
public class V5 implements Cloneable {

	public static V5 Y = new V5(0, 1, 0, 0, 0);

	public double x;
	public double y;
	public double z;
	public float yaw;
	public float pitch;

	public V5 nullify() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.yaw = 0;
		this.pitch = 0;
		return this;
	}

	public V5 add(V5 that) {
		if (that == null) return this;
		this.x += that.x;
		this.y += that.y;
		this.z += that.z;
		this.yaw += that.yaw;
		this.pitch += that.pitch;
		return this;
	}

	public V5 add(double x, double y, double z) {
		return add(x, y, z, 0);
	}

	public V5 add(double x, double y, double z, float yaw) {
		this.x += x;
		this.y += y;
		this.z += z;
		this.yaw += yaw;
		this.yaw %= 360;
		return this;
	}

	public V5 sum(V5 that) {
		if (that == null) return this.clone();
		return new V5(
				this.x + that.x,
				this.y + that.y,
				this.z + that.z,
				this.yaw + that.yaw,
				this.pitch + that.pitch
		);
	}

	public V5 posSum(V5 that) {
		if (that == null) return this.clone();
		return new V5(
				this.x + that.x,
				this.y + that.y,
				this.z + that.z,
				this.yaw,
				this.pitch
		);
	}

	public static V5 sum(V5 a, V5 b) {
		return a == null ? b : a.sum(b);
	}

	public V5 rotate(V5 axis, float degrees) {
		if (axis.y == 0 || axis.x != 0 || axis.z != 0)
			// ToDo: Implement quaternion rotation
			throw new UnsupportedOperationException("Non-Y axis rotations are yet to be implemented.");

		double prevX = this.x;
		double prevZ = this.z;
		double radians = Math.toRadians(degrees);
		this.x = prevX * cos(radians) - prevZ * sin(radians);
		this.z = prevX * sin(radians) + prevZ * cos(radians);
		this.yaw += degrees;
		return this;
	}

	public Vector toVector() {
		return new Vector(x, y, z);
	}

	public static V5 fromVector(Vector vec) {
		return new V5(vec.x, vec.y, vec.z, 0, 0);
	}

	public Location toLocation(World world) {
		return new Location(world, x, y, z, yaw, pitch);
	}
	public static V5 fromLocation(Location loc) {
		return new V5(loc.x, loc.y, loc.z, loc.yaw, loc.pitch);
	}

	@Override
	public V5 clone() {
		try {
			return (V5) super.clone();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public String toString() {
		return "[" + s(x) + ", " + s(y) + ", " + s(z) + "] (" + s(yaw) + "deg)";
	}

	private static String s(double d) {
		return (int) (d * 10) / 10.0 + "";
	}

}
