package ru.cristalix.museum.museum.map;

import lombok.Data;
import org.bukkit.Location;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.subject.Subject;

@Data
public class SubjectPrototype {

	private final String address;
	private final double price;
	private final Provider provider;
	private final Location pointMin;
	private final Location pointMax;
	private final V3 relativeOrigin;
	private final V3 dimensions;

	public interface Provider {
		Subject provide(Museum museum, SubjectInfo info);
	}

}
