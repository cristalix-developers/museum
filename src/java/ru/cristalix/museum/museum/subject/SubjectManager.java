package ru.cristalix.museum.museum.subject;

import clepto.cristalix.Box;
import clepto.cristalix.WorldMeta;
import org.bukkit.Location;
import ru.cristalix.museum.Manager;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.museum.map.SubjectType;

import java.util.stream.Collectors;

/**
 * @author func 22.06.2020
 * @project museum
 */
public class SubjectManager extends Manager<SubjectPrototype> {

    public SubjectManager() {
        super("subject");
    }

    @Override
    protected SubjectPrototype readBox(String address, Box box) {
        String typeStr = box.requireLabel("type").getTag();
        Location origin = box.getLabels("origin").stream()
                .findAny()
                .orElse(null);

        if (origin == null)
            origin = box.getCenter();

        return new SubjectPrototype(
                address,
                SubjectType.byString(typeStr),
                box.requireLabel("price").getTagDouble(),
                box,
                box.toRelativeVector(origin),
                box.getLabels("manipulator").stream()
                        .map(box::toRelativeVector)
                        .collect(Collectors.toList())
        );
    }
}
