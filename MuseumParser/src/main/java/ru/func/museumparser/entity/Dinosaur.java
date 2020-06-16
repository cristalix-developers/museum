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
public class Dinosaur {
    private String title;
    private Fragment[] subs;
    private ElementRare rare;

    public Dinosaur(Location zeroPoint, double radius, ElementRare rare, String title) {
        subs = new Fragment[]{null};
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
                    for (Fragment fragment : subs) {
                        if (fragment == null)
                            continue;
                        if (fragment.getTitle().equals(name)) {
                            founded = true;
                            Piece[] pieces = new Piece[fragment.getPieces().length + 1];
                            // Мне не нравится, это перезиписывание массива, если в начале есть будет, то из-за этого
                            System.arraycopy(fragment.getPieces(), 0, pieces, 0, fragment.getPieces().length);
                            pieces[fragment.getPieces().length] = piece;
                            fragment.setPieces(pieces);
                        }
                    }
                    // Если родитель не нашелся - то стать самому себе родителем.
                    if (!founded) {
                        if (subs[0] != null) {
                            Fragment[] temp = new Fragment[subs.length + 1];
                            System.arraycopy(subs, 0, temp, 0, subs.length);
                            subs = temp;
                        }
                        subs[subs.length - 1] = new Fragment(name, new Piece[]{piece});
                    }
                });
        this.title = title;
        this.rare = rare;
    }
}
