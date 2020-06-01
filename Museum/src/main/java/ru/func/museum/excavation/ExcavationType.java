package ru.func.museum.excavation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import ru.func.museum.excavation.generator.HalfSphereGenerator;

@Getter
@AllArgsConstructor
public enum ExcavationType {
    NOOP(null),
    DIRT(new DefaultExcavation(
            new HalfSphereGenerator(
                    new Location(Excavation.WORLD, -72, 88, 260),
                    10,
                    new Material[]{
                            Material.SANDSTONE,
                            Material.SAND,
                            Material.RED_SANDSTONE,
                    }, 0
            ), "Раскопки древнего динозавра",
            100,
            1,
            new Location(Excavation.WORLD, -50, 83, 242),
            50
    )),
    ;

    private Excavation excavation;
}
