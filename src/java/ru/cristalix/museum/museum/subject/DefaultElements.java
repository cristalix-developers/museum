package ru.cristalix.museum.museum.subject;

import ru.cristalix.museum.data.MuseumInfo;
import ru.cristalix.museum.data.PickaxeType;
import ru.cristalix.museum.data.SubjectInfo;
import ru.cristalix.museum.data.UserInfo;
import ru.cristalix.museum.museum.map.MuseumPrototype;
import ru.cristalix.museum.prototype.Managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

public class DefaultElements {

	public static UserInfo createNewUserInfo(UUID userId) {
		MuseumPrototype proto = Managers.museum.getPrototype("main");
		MuseumInfo startMuseum = new MuseumInfo(
				proto.getAddress(),
				"Музей археологии",
				new Date(),
				3
		);

		UserInfo userInfo = new UserInfo(
				userId,
				0,
				1000.0,
				PickaxeType.DEFAULT,
				Collections.singletonList(startMuseum),
				new ArrayList<>(),
				new ArrayList<>(),
				0,
				0,
				new ArrayList<>(),
				new ArrayList<>()
		);

		for (SubjectInfo subject : proto.getDefaultSubjects())
			userInfo.getSubjectInfos().add(subject.duplicate());

		return userInfo;
	}

}
