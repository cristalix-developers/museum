package museum.museum.map;

import clepto.cristalix.mapservice.Box;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import museum.data.SubjectInfo;
import museum.museum.subject.Subject;
import museum.player.User;
import museum.prototype.Prototype;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.math.V3;

import java.util.List;

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

		private final String title;
		private final V3 min;
		private final V3 max;
		private final double cost;

	}
}
