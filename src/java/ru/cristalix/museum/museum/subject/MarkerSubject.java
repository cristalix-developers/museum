package ru.cristalix.museum.museum.subject;

import org.bukkit.Location;
import ru.cristalix.core.util.UtilV3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.player.User;

public class MarkerSubject implements Subject {

	private final SubjectInfo info;
	private final Location location;

	public MarkerSubject(Museum museum, SubjectInfo subjectInfo, SubjectPrototype prototype) {
		this.info = subjectInfo;
		this.location = UtilV3.toLocation(subjectInfo.getLocation(), App.getApp().getWorld());
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public void show(User user) {

	}

	@Override
	public void hide(User user) {

	}

	@Override
	public SubjectInfo generateInfo() {
		return info;
	}

}
