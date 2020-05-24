package ru.func.museum.excavation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.excavation.generator.GeneratorType;

@Getter
@AllArgsConstructor
public enum ExcavationType {
    NOOP(null, null),
    DIRT(new DefaultExcavation(
            "Раскопки древнего динозавра",
            100,
            1,
            new Location(Excavation.WORLD, -50, 83, 242),
            new Location(Excavation.WORLD, -50, 83, 242),
            25,
            50
    ), GeneratorType.DEFAULT),;

    private Excavation excavation;
    private GeneratorType generatorType;
}
