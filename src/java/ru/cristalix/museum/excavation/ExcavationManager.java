package ru.cristalix.museum.excavation;

import clepto.ListUtils;
import clepto.bukkit.InvalidConfigException;
import clepto.cristalix.WorldMeta;
import ru.cristalix.museum.museum.subject.skeleton.SkeletonPrototype;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import ru.cristalix.core.build.models.Box;
import ru.cristalix.core.build.models.IZone;
import ru.cristalix.core.build.models.Point;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.museum.map.MuseumManager;
import ru.cristalix.museum.player.User;

import java.util.*;

public class ExcavationManager {

	private final Map<String, ExcavationPrototype> excavationPrototypeMap = new HashMap<>();

	public static boolean isAir(User user, BlockPosition pos) {
		return user.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()).getType() == Material.AIR;
	}

	public ExcavationPrototype getExcavationPrototype(String address) {
		return excavationPrototypeMap.get(address);
	}

	public ExcavationManager(App app, WorldMeta meta) {
		for (Map.Entry<String, IZone> entry : meta.getMeta().getZones().entrySet()) {
			String key = entry.getKey();
			if (!key.startsWith("excavation")) continue;
			IZone zone = entry.getValue();
			if (!(zone instanceof Box)) continue;
			Box box = (Box) zone;
			V3 min = box.getMin();
			V3 max = box.getMax();
			min.setY(0);
			max.setY(255);
			MuseumManager map = app.getMuseumManager();
			double price = Double.parseDouble(map.requireTagInZone("price", zone));
			int hitCount = Integer.parseInt(map.requireTagInZone("hit-count", zone));
			int requredLevl = Integer.parseInt(map.requireTagInZone("required-level", zone));
			String title = map.requireTagInZone("title", zone);
			Location spawn = map.point2Loc(map.getPointsInZone("spawn", zone).iterator().next());
			List<int[]> pallette = new ArrayList<>();
			for (Point p : map.getPointsInZone("pallette", zone)) {
				System.out.println("Pallette point: " + p.getV3());
				Block block = meta.point2Loc(p).add(0, -1, 0).getBlock();
				pallette.add(new int[] {block.getType().getId(), block.getData()});
			}
			if (pallette.isEmpty()) {
				throw new InvalidConfigException("No pallette markers found for excavation " + meta.getMeta().getName() + "/" + key);
			}

			List<SkeletonPrototype> skeletonPrototypes = new ArrayList<>();
			for (Point p : map.getPointsInZone("available", zone)) {
				skeletonPrototypes.add(app.getSkeletonManager().getExhibit(p.getTag()));
			}
			if (skeletonPrototypes.isEmpty()) {
				throw new InvalidConfigException("No available skeletons (.p available) found for excavation " +
						meta.getMeta().getName() + "/" + key);
			}

			List<Location> space = new ArrayList<>();
			for (int x = (int) min.getX(); x < (int) max.getX(); x++) {
				for (int y = (int) min.getY(); y < (int) max.getY(); y++) {
					for (int z = (int) min.getZ(); z < (int) max.getZ(); z++) {
						Location loc = new Location(meta.getWorld(), x, y, z);
						Block block = loc.getBlock();
						if (block.getType() == Material.IRON_BLOCK) space.add(loc);
					}
				}
			}

			Set<Chunk> chunks = new HashSet<>();
			for (Location location : space) {
				Block block = location.getBlock();
				int[] random = ListUtils.random(pallette);
				block.setTypeIdAndData(random[0], (byte) random[1], false);
				chunks.add(location.getChunk());
			}

			List<PacketPlayOutMapChunk> packets = new ArrayList<>();
			for (Chunk chunk : chunks) {
				packets.add(new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535));
			}

			ExcavationPrototype prototype = new ExcavationPrototype(key.replace("excavation", ""), skeletonPrototypes, hitCount, requredLevl, price, title, spawn, packets);
			excavationPrototypeMap.put(prototype.getAddress(), prototype);

		}
	}

}
