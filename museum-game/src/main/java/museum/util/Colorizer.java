package museum.util;

import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.IBlockData;
import ru.cristalix.core.formatting.Color;

@UtilityClass
public class Colorizer {

	public static IBlockData applyColor(IBlockData data, Color color) {
		Block block = data.getBlock();
		// concrete и concrete_powder меняют цвет
		return block == Blocks.dR || block == Blocks.dS ? block.fromLegacyData(color.getWoolData()) : data;
	}

}
