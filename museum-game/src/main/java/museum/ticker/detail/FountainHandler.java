package museum.ticker.detail;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;
import museum.App;
import museum.ticker.Ticked;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author func 15.07.2020
 * @project museum
 */
public class FountainHandler implements Ticked {

	private Random random = new Random();
	private final List<Fountain> fountains;

	public FountainHandler(App app) {
		// Формат таблички: .p fountain <x> <y> <z> (power vector) <x> <y> <z> (noise vector)
		fountains = app.getMap().getLabels("fountain").stream()
				.map(label -> {
					String[] tag = label.getTag().split("\\s+");
					return new Fountain(
							label,
							new Vector(Double.parseDouble(tag[0]), Double.parseDouble(tag[1]), Double.parseDouble(tag[2])),
							new Vector(Double.parseDouble(tag[3]), Double.parseDouble(tag[4]), Double.parseDouble(tag[5]))
					);
				}).collect(Collectors.toList());
	}

	@Override
	public void tick(int... args) {
		if (args[0] % 3 != 0)
			return;
		for (Fountain fountain : fountains)
			fountain.spawnParticle();
	}

	@AllArgsConstructor
	class Fountain {
		private Location location;
		private Vector vector;
		private Vector noise;

		public void spawnParticle() {
			FallingBlock particle = location.getWorld().spawnFallingBlock(location, Material.STAINED_GLASS, (byte) 3);
			particle.setDropItem(false);
			particle.setVelocity(vector.clone().add(new Vector(
					(random.nextDouble() - .5) * noise.getX(),
					(random.nextDouble() - .5) * noise.getY(),
					(random.nextDouble() - .5) * noise.getZ()
			)));
		}
	}
}
