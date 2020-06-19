package ru.cristalix.museum.museum.subject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.player.User;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SimpleSubject implements Subject {

	protected final SubjectPrototype prototype;
	protected final Museum museum;
	protected final SubjectInfo info;
	protected final Location location;

	public SimpleSubject(Museum museum, SubjectInfo info) {
		this.museum = museum;
		this.info = info;
		V3 loc = info.getLocationDelta();
		this.location = museum.getPrototype().getOrigin().clone().add(loc.getX(), loc.getY(), loc.getZ());
		this.prototype = museum.getPrototype().getMap().getSubjectPrototype(info.prototypeAddress);
	}

	@Override
	public void show(User owner) {

	}

	@Override
	public void hide(User owner) {

	}

	@Override
	public SubjectInfo generateInfo() {
		return info;
	}

}
