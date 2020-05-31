package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.EulerAngle;

/**
 * @author func 31.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SubEntity {
    private int groupId;
    private String title;
    private double vectorX;
    private double vectorY;
    private double vectorZ;
    private EulerAngle headRotation;
}
