package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.func.museum.museum.space.Space;
import ru.func.museum.museum.space.SpaceType;

import java.util.Optional;

@Getter
@AllArgsConstructor
public class Element {
    // То, где расположен элемент
    private Space space;
    // Тип поля, на который можно поставить элемент
    private SpaceType spaceType;
    private double cost;
    private String title;
}
