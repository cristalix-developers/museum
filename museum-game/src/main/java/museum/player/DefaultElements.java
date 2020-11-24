package museum.player;

import lombok.experimental.UtilityClass;
import museum.data.model.Model;
import museum.data.model.MuseumModel;
import museum.data.model.SubjectModel;
import museum.data.UserInfo;
import museum.museum.map.MuseumPrototype;
import museum.prototype.Managers;

import java.util.*;

@UtilityClass
public class DefaultElements {

	public static UserInfo createNewUserInfo(UUID id, String name) {
		MuseumPrototype proto = Managers.museum.getPrototype("main");
		MuseumModel startMuseum = new MuseumModel(
				UUID.randomUUID(),
				proto.getAddress(),
				new Date(),
				"Музей имени " + name,
				0
		);

		List<Model> models = new ArrayList<>();

		models.add(startMuseum);

		for (SubjectModel defaultSubject : proto.getDefaultSubjects()) {
			models.add(defaultSubject.duplicate());
		}

		return new UserInfo(
				id,
				null,
				0,
				false,
				0,
				1000.0,
				0,
				0,
				0,
				0,
				null,
				models,
				new ArrayList<>(),
				new ArrayList<>()
		);
	}

}
