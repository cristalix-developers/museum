package ru.func.museum.excavation.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum GeneratorType {
    DEFAULT(new DefaultGenerator()),;

    private ExcavationGenerator excavationGenerator;
}
