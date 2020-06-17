package ru.func.museum.excavation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import ru.func.museum.excavation.generator.HalfSphereGenerator;

@Getter
@AllArgsConstructor
public enum ExcavationType {
    NOOP(null, null),
    DIRT(new DefaultExcavation(
            new HalfSphereGenerator(
                    new Location(Excavation.WORLD, -72, 88, 260),
                    10,
                    10,
                    new Material[]{
                            Material.SANDSTONE,
                            Material.SAND,
                            Material.RED_SANDSTONE,
                    }, 0
            ), "раскопки древнего динозавра",
            100,
            1,
            new Location(Excavation.WORLD, -71, 88, 273),
            50
    ), Material.DIRT),
    ;

    private Excavation excavation;
    private Material icon;
}
