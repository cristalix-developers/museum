package ru.func.museumparser.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;

/**
 * @author func 31.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SubEntity {
    private String title;
    @Setter
    private Piece[] pieces;
}
