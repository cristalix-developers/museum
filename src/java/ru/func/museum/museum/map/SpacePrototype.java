package ru.func.museum.museum.map;

import lombok.Data;
import org.bukkit.Location;
import ru.func.museum.museum.hall.template.space.Space;

import java.util.function.Supplier;

@Data
public class SpacePrototype {

	private final Location manipulator;
	private final Supplier<Space> spaceSupplier;
	private final int size;

}
