package museum.player;

import lombok.experimental.UtilityClass;
import museum.data.MuseumInfo;
import museum.data.PickaxeType;
import museum.data.SubjectInfo;
import museum.data.UserInfo;
import museum.museum.map.MuseumPrototype;
import museum.prototype.Managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@UtilityClass
public class DefaultElements {

	public static UserInfo createNewUserInfo(UUID userId) {
		MuseumPrototype proto = Managers.museum.getPrototype("main");
		MuseumInfo startMuseum = new MuseumInfo(
				proto.getAddress(),
				"Музей археологии",
				new Date(),
				0
		);

		UserInfo userInfo = new UserInfo(
				userId,
				0,
				1000.0,
				0L,
				PickaxeType.DEFAULT,
				Collections.singletonList(startMuseum),
				new ArrayList<>(),
				new ArrayList<>(),
				0,
				0,
				null,
				new ArrayList<>(),
				new ArrayList<>(),
				0
		);

		for (SubjectInfo subject : proto.getDefaultSubjects())
			userInfo.getSubjectInfos().add(subject.duplicate());

		return userInfo;
	}

}
