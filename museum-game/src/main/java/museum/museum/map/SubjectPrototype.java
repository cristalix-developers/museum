package museum.museum.map;

import clepto.bukkit.DynamicItem;
import clepto.cristalix.mapservice.Box;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import museum.data.SubjectInfo;
import museum.museum.subject.Subject;
import museum.player.User;
import museum.prototype.Prototype;
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
	private final DynamicItem icon;

	public Subject provide(SubjectInfo info, User owner) {
		return type.provide(this, info, owner);
	}

}
