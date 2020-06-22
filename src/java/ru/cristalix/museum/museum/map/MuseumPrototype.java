package ru.cristalix.museum.museum.map;

import lombok.Data;
import org.bukkit.Location;
import ru.cristalix.museum.Prototype;

import java.util.*;

@Data
public class MuseumPrototype implements Prototype {

	private final String address;
	private final MuseumManager map;
	private final Location spawnPoint;
	private final List<SubjectPrototype> defaultSubjects;

}
