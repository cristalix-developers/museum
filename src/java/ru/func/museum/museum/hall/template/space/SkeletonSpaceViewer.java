package ru.func.museum.museum.hall.template.space;

import lombok.*;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.Vector3f;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import ru.func.museum.App;
import ru.func.museum.element.Element;
import ru.func.museum.element.deserialized.Piece;
import ru.func.museum.player.User;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.List;
import java.util.Random;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Setter
@Getter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class SkeletonSpaceViewer implements Space {
    @NonNull
    private int startDotX;
    @NonNull
    private int startDotY;
    @NonNull
    private int startDotZ;
    @NonNull
    private SpaceReflectType reflection;
    @NonNull
    private int entity;
    private transient int seed;
    private transient int amount = 0;
    private transient Random random;
    @NonNull
    private int x;
    @NonNull
    private int y;
    @NonNull
    private int z;
    @NonNull
    private List<Element> elements;
    @NonNull
    private List<Integer> accessEntities;

    @Override
    public boolean isManipulator(Location location) {
        return Math.abs(location.getBlockX() - x) < 2 &&
                Math.abs(location.getBlockY() - y) < 2 &&
                Math.abs(location.getBlockZ() - z) < 2;
    }

    @Override
    public void show(User owner, Player guest) {
        if (entity < 0)
            return;

        seed = Pickaxe.RANDOM.nextInt(999);
        random = new Random(seed);

        val subEntities = App.getApp().getMuseumEntities()[entity].getSubs();

        for (int i = 0; i < subEntities.length; i++) {
            for (Element element : elements) {
                if (element.getParentId() == entity && i == element.getId()) {
                    for (Piece piece : subEntities[i].getPieces()) {
                        EulerAngle angle = piece.getHeadRotation();
                        piece.single(
                                ((CraftPlayer) guest).getHandle().playerConnection,
                                subEntities[i].getTitle(),
                                reflection.rotate(new Location(
                                        guest.getWorld(),
                                        startDotX,
                                        startDotY,
                                        startDotZ
                                ), piece),
                                new Vector3f(
                                        (float) angle.getX(),
                                        (float) angle.getY(),
                                        (float) angle.getZ()
                                ), 0, 0, 0,
                                random.nextInt(1000) + 1000
                        );
                        amount++;
                    }
                }
            }
        }
    }

    @Override
    public void hide(User owner, Player guest) {
        int[] ids = new int[amount];
        amount = 0;
        random.setSeed(seed);
        for (int i = 0; i < ids.length; i++)
            ids[i] = random.nextInt(1000) + 1000;
        ((CraftPlayer) guest).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(ids));
        amount = 0;
    }

    @Override
    public List<Element> getElements() {
        return elements;
    }
}
