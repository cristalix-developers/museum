package ru.cristalix.museum.museum.subject;

import org.bukkit.Location;
import ru.cristalix.core.util.UtilV3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.prototype.Managers;

public class MarkerSubject implements Subject {

	private final SubjectInfo info;
	private final Location location;

	public MarkerSubject(Museum museum, SubjectInfo info, SubjectPrototype prototype) {
		this.info = info;
		this.location = UtilV3.toLocation(info.getLocation(), App.getApp().getWorld());
	}

	public String getCollectorAddress() {
		return info.getMetadata();
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
	public SubjectType<?> getType() {
		return SubjectType.MARKER;
	}

	@Override
	public SubjectInfo generateInfo() {
		return info;
	}

}
