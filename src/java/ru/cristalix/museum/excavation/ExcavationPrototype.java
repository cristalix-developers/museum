package ru.cristalix.museum.excavation;

import ru.cristalix.museum.Prototype;
import ru.cristalix.museum.museum.subject.skeleton.SkeletonPrototype;
import lombok.Data;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import org.bukkit.Location;

import java.util.List;

@Data
public class ExcavationPrototype implements Prototype {

	private final String address;
	private final List<SkeletonPrototype> availableSkeletonPrototypes;
	private final int hitCount;
	private final int requiredLevel;
	private final double price;
	private final String title;
	private final Location spawnPoint;
	private final List<PacketPlayOutMapChunk> packets;

}
