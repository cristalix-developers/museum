package ru.cristalix.museum.museum.map;

import lombok.Data;
import org.bukkit.Location;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.prototype.Prototype;

import java.util.*;

@Data
public class MuseumPrototype implements Prototype {

	private final String address;
	private final Location spawnPoint;
	private final List<SubjectInfo> defaultSubjects;

}
