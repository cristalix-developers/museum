package ru.func.museum.element;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

/**
 * @author func 31.05.2020
 * @project Museum
 */
@Getter
public class MuseumEntity {
    private String title;
    private SubEntity[] subs;
    private ElementRare rare;

    public MuseumEntity(Location zeroPoint, double dzRadius, double yRadius, String title, ElementRare rare) {
        subs = zeroPoint.getNearbyLivingEntities(dzRadius, yRadius, entity -> entity.getType().equals(EntityType.ARMOR_STAND))
                .stream()
                .map(entity -> (ArmorStand) entity)
                .map(entity -> new SubEntity(
                        Integer.parseInt(entity.getCustomName().split("|")[0]),
                        entity.getCustomName().split("|")[1],
                        zeroPoint.getX() + entity.getLocation().getX(),
                        zeroPoint.getY() + entity.getLocation().getY(),
                        zeroPoint.getZ() + entity.getLocation().getZ(),
                        entity.getHeadPose()
                )).toArray(SubEntity[]::new);
        this.title = title;
        this.rare = rare;
    }
}
