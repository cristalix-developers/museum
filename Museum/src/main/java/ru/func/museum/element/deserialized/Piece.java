package ru.func.museum.element.deserialized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;

/**
 * @author func 01.06.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class Piece {
    private double vectorX;
    private double vectorY;
    private double vectorZ;
    private EulerAngle headRotation;
    private Material material;
}
