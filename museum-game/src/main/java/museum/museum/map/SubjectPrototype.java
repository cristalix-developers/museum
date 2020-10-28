package museum.museum.map;

import clepto.bukkit.world.Box;
import clepto.math.V3;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import museum.data.SubjectInfo;
import museum.museum.subject.Subject;
import museum.player.User;
import museum.prototype.Prototype;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
public class SubjectPrototype implements Prototype {

	private final String address;
	private final SubjectType<?> type;
	private final double price;
	private final Box box;
	private final String title;
	private final int cristalixPrice;
	private final V3 relativeOrigin;
	private final List<V3> relativeManipulators;
	private final ItemStack icon;
	private final Material able;
	private final SubjectDataForClient dataForClient;

	public Subject provide(SubjectInfo info, User owner) {
		return type.provide(this, info, owner);
	}

	@Data
	public static class SubjectDataForClient {

		private final String address;
		private final String title;
		private final ru.cristalix.core.math.V3 min;
		private final ru.cristalix.core.math.V3 max;
		private final double cost;
		private final UUID uuid;

	}
}
