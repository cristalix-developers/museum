package ru.func.museumparser.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;

/**
 * @author func 01.06.2020
 * @project MuseumParser
 */
@Getter
@Setter
@AllArgsConstructor
public class Piece {
    private double vectorX;
    private double vectorY;
    private double vectorZ;
    private EulerAngle headRotation;
    private Material material;
}
