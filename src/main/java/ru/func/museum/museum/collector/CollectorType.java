package ru.func.museum.museum.collector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import ru.func.museum.museum.Museum;

@Getter
@AllArgsConstructor
public enum CollectorType {
    DEFAULT(new DefaultCollector());

    private AbstractCollector collector;
}
