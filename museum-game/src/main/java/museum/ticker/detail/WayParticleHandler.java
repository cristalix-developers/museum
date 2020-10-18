package museum.ticker.detail;

import clepto.bukkit.world.Label;
import lombok.AllArgsConstructor;
import lombok.val;
import museum.App;
import museum.ticker.Ticked;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author func 19.09.2020
 * @project museum
 */
public class WayParticleHandler implements Ticked {

	private final List<ParticleLine> line;

	public WayParticleHandler(App app) {
		// Формат таблички: .p line <number>
		List<Label> lines = app.getMap().getLabels("line");
		lines.sort(Comparator.comparingInt(Label::getTagInt));

		List<ParticleLine> prototype = new ArrayList<>();

		for (int i = 0; i <= lines.size() / 2; i += 2) {
			Location start;
			Location end;
			if (lines.get(i).lengthSquared() > lines.get(i+1).lengthSquared()) {
				end = lines.get(i);
				start = lines.get(i+1);
			} else {
				start = lines.get(i);
				end = lines.get(i+1);
			}
			val chunk = start.getChunk();
			Chunk[] chunks = new Chunk[9];
			int j = 0;
			for (int x = chunk.getX() - 1; x <= chunk.getX() + 1; x++)
				for (int z = chunk.getZ() - 1; z <= chunk.getZ() + 1; z++)
					chunks[j++] = chunk.getWorld().getChunkAt(x, z);
			prototype.add(new ParticleLine(start, end, chunks));
		}
		line = prototype;
	}

	@Override
	public void tick(int... args) {
		for (ParticleLine line : this.line)
			line.draw(20, args[0] % 20);
	}

	@AllArgsConstructor
	static class ParticleLine {

		private final Location start;
		private final Location end;
		private final Chunk[] chunks;

		public void draw(double speed, double time) {
			for (Chunk chunk : chunks)
				if (!chunk.isLoaded())
					return;

			double dx = (end.x - start.x) / speed * time;
			double dy = (end.y - start.y) / speed * time + .2;
			double dz = (end.z - start.z) / speed * time;

			start.getWorld().spawnParticle(Particle.SPELL_INSTANT, start.clone().add(dx, dy, dz), 1);
		}
	}
}
