package ru.func.museum.element.deserialized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.Vector3f;
import org.bukkit.Location;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.List;

/**
 * @author func 31.05.2020
 * @project Museum
 */
@Getter
@AllArgsConstructor
public class SubEntity {
    private List<Piece> pieces;
    private String title;

    public void show(PlayerConnection connection, Location location, int parentId, int subEntity) {
        int i = 0;

        float randomXAngle = Pickaxe.RANDOM.nextFloat() * 360;
        float randomYAngle = Pickaxe.RANDOM.nextFloat() * 360;
        float randomZAngle = Pickaxe.RANDOM.nextFloat() * 360;

        int noise = 1 + Pickaxe.RANDOM.nextInt(9);
        for(Piece piece : pieces) {
            piece.single(
                    connection,
                    title,
                    location,
                    new Vector3f(randomXAngle, randomYAngle, randomZAngle),
                    noise,
                    parentId,
                    subEntity,
                    i
            );
            i++;
        }
    }
}
