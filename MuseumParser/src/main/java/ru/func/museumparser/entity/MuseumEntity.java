package ru.func.museumparser.entity;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author func 31.05.2020
 * @project Museum
 */
@Getter
public class MuseumEntity {
    private String title;
    private SubEntity[] subs;
    private ElementRare rare;

    public MuseumEntity(Location zeroPoint, double radius, ElementRare rare, String title) {
        subs = new SubEntity[]{null};
        zeroPoint.getNearbyLivingEntities(radius, entity -> entity.getType().equals(EntityType.ARMOR_STAND)).stream()
                .map(entity -> (ArmorStand) entity)
                .forEach(entity -> {
                    // Создаем кусочек по сущности
                    Piece piece = new Piece(
                            zeroPoint.getBlockX() - entity.getLocation().getX(),
                            zeroPoint.getBlockY() - entity.getLocation().getY(),
                            zeroPoint.getBlockZ() - entity.getLocation().getZ(),
                            entity.getHeadPose(),
                            entity.getHelmet().getType()
                    );

                    boolean founded = false;
                    String name = entity.getCustomName();

                    // Если надсущность уже определена, примкнуть кусочку, как часть
                    for (SubEntity subEntity : subs) {
                        if (subEntity == null)
                            continue;
                        if (subEntity.getTitle().equals(name)) {
                            founded = true;
                            Piece[] pieces = new Piece[subEntity.getPieces().length + 1];
                            // Мне не нравится, это перезиписывание массива, если в начале есть будет, то из-за этого
                            System.arraycopy(subEntity.getPieces(), 0, pieces, 0, subEntity.getPieces().length);
                            pieces[subEntity.getPieces().length] = piece;
                            subEntity.setPieces(pieces);
                        }
                    }
                    // Если родитель не нашелся - то стать самому себе родителем.
                    if (!founded) {
                        if (subs[0] != null) {
                            SubEntity[] temp = new SubEntity[subs.length + 1];
                            System.arraycopy(subs, 0, temp, 0, subs.length);
                            subs = temp;
                        }
                        subs[subs.length - 1] = new SubEntity(name, new Piece[]{piece});
                    }
                });
        this.title = title;
        this.rare = rare;
    }
}
