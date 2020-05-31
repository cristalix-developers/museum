package ru.func.museumparser.entity;

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

    public MuseumEntity(Location zeroPoint, double dzRadius, double yRadius, ElementRare rare, String title) {
        subs = zeroPoint.getNearbyLivingEntities(dzRadius, yRadius, entity -> entity.getType().equals(EntityType.ARMOR_STAND))
                .stream()
                .map(entity -> (ArmorStand) entity)
                .map(entity -> new SubEntity(
                        Integer.parseInt(entity.getCustomName().replace(" ", "").split(">")[0]),
                        entity.getCustomName().split(">")[1],
                        zeroPoint.getBlockX() - entity.getLocation().getX(),
                        zeroPoint.getBlockY() - entity.getLocation().getY(),
                        zeroPoint.getBlockZ() - entity.getLocation().getZ(),
                        entity.getHeadPose(),
                        entity.getHelmet().getType()
                )).toArray(SubEntity[]::new);
        this.title = title;
        this.rare = rare;
    }
}
