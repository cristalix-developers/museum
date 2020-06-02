package ru.func.museum.museum.space;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.Vector3f;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import ru.func.museum.App;
import ru.func.museum.element.Element;
import ru.func.museum.element.deserialized.Piece;
import ru.func.museum.element.deserialized.SubEntity;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.List;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SkeletonSpaceViewer implements Space {

    private int startDotX;
    private int startDotY;
    private int startDotZ;
    private SpaceReflectType reflection;
    private int entity;

    @Override
    public void show(Archaeologist owner, Player guest) {
        if (entity < 0)
            return;
        SubEntity[] subEntities = App.getApp().getMuseumEntities()[entity].getSubs();
        for (int i = 0; i < subEntities.length; i++) {
            List<Piece> pieces = subEntities[i].getPieces();
            for (int j = 0; j < pieces.size(); j++) {
                for (Element element : owner.getElementList()) {
                    if (element.getParentId() == entity && element.getId() == i) {
                        EulerAngle angle = pieces.get(j).getHeadRotation();
                        pieces.get(j).single(
                                ((CraftPlayer) guest).getHandle().playerConnection,
                                subEntities[i].getTitle(),
                                reflection.rotate(new Location(
                                        guest.getWorld(),
                                        startDotX,
                                        startDotY,
                                        startDotZ
                                ), pieces.get(j)),
                                new Vector3f(
                                        (float) angle.getX(),
                                        (float) angle.getY(),
                                        (float) angle.getZ()
                                ), 0, 0, 0,
                                Pickaxe.RANDOM.nextInt(1_000_000)
                        );
                    }
                }
            }
        }
    }
}
