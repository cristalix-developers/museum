package ru.func.museum.museum.map;

import lombok.Data;
import org.bukkit.Location;
import ru.func.museum.museum.hall.template.space.Subject;

import java.util.function.Supplier;

@Data
public class SpacePrototype {

	private final Location manipulator;
	private final Supplier<Subject> spaceSupplier;
	private final int size;

}
