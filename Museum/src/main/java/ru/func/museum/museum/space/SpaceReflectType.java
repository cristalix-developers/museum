package ru.func.museum.museum.space;

import lombok.AllArgsConstructor;
import org.bukkit.Location;
import ru.func.museum.element.deserialized.Piece;

@AllArgsConstructor
public enum SpaceReflectType {
    SOUTH(-1, 1, 0),
    WEST(-1, -1, 90),
    NORTH(1, -1, 180),
    EAST(1, 1, 270),
    ;

    private int dX;
    private int dZ;
    private float delta;

    public Location rotate(Location startDot, Piece piece) {
        return new Location(
                startDot.getWorld(),
                startDot.getBlockX() + piece.getVectorX() * dX,
                startDot.getBlockY(),
                startDot.getBlockZ() + piece.getVectorZ() * dZ,
                delta,
                0
        );
    }
}
