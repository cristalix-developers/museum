package museum.player;

import lombok.experimental.UtilityClass;
import museum.data.MuseumInfo;
import museum.data.PickaxeType;
import museum.data.SubjectInfo;
import museum.data.UserInfo;
import museum.museum.map.MuseumPrototype;
import museum.prototype.Managers;

import java.util.*;

@UtilityClass
public class DefaultElements {

	public static UserInfo createNewUserInfo(UUID userId) {
		MuseumPrototype proto = Managers.museum.getPrototype("main");
		MuseumInfo startMuseum = new MuseumInfo(
				proto.getAddress(),
				"Музей палеантологии",
				new Date(),
				0
		);

		UserInfo userInfo = new UserInfo(
				userId,
				null,
				0,
				700.0,
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
				new ArrayList<>(),
				new ArrayList<>(),
				0,
				false,
				0,
				1,
				-1L,
				0,
				new ArrayList<>(),
				false,
				true,
				0,
				0,
				new ArrayList<>(Arrays.asList(
						"ADDITIONAL_EXP:0",
						"EXTRA_HITS:0",
						"EFFICIENCY:0",
						"BONE_DETECTION:0",
						"DETECTION_OF_RELIQUES:0",
						"DUPLICATE:0"
				))
		);

		for (SubjectInfo subject : proto.getDefaultSubjects())
			userInfo.getSubjectInfos().add(subject.duplicate());

		return userInfo;
	}
}