package museum.museum.map;

import clepto.cristalix.mapservice.Box;
import lombok.Data;
import museum.data.SubjectInfo;
import museum.prototype.Prototype;

import java.util.List;

@Data
public class MuseumPrototype implements Prototype {

	private final String address;
	private final Box box;
	private final List<SubjectInfo> defaultSubjects;

}
