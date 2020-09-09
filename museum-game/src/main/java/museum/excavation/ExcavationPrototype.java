package museum.excavation;

import lombok.Data;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.prototype.Prototype;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import org.bukkit.Material;

import java.util.List;

@Data
public class ExcavationPrototype implements Prototype {

	private final String address;
	private final List<SkeletonPrototype> availableSkeletonPrototypes;
	private final int hitCount;
	private final int requiredLevel;
	private final double price;
	private final String title;
	private final List<PacketPlayOutMapChunk> packets;
	private final Material icon;

}
