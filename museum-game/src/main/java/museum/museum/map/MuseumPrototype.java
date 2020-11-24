package museum.museum.map;

import clepto.bukkit.world.Box;
import lombok.Data;
import museum.data.model.SubjectModel;
import museum.prototype.Prototype;
import org.bukkit.Location;

import java.util.List;

@Data
public class MuseumPrototype implements Prototype {

	private final String address;
	private final Box box;
	private final Location spawn;
	private final List<SubjectModel> defaultSubjects;

}
