package ru.func.museum.museum.hall.template.space;

import delfikpro.exhibit.Exhibit;
import lombok.*;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.Vector3f;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import ru.cristalix.core.math.V3;
import ru.func.museum.App;
import ru.func.museum.data.subject.SubjectInfo;
import ru.func.museum.element.Element;
import ru.func.museum.element.deserialized.Piece;
import ru.func.museum.museum.Museum;
import ru.func.museum.player.User;

import java.util.List;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Setter
@Getter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class SkeletonSubject implements Subject {

	private final Museum museum;
	private final SubjectInfo subjectInfo;
	private Location location;
    private List<Element> elements;
    private Exhibit exhibit;
    private float yaw;

    public SkeletonSubject(Museum museum, SubjectInfo info) {
    	this.museum = museum;
    	this.subjectInfo = info;
		V3 loc = info.getLocationDelta();
		this.location = museum.getPrototype().getOrigin().clone().add(loc.getX(), loc.getY(), loc.getZ());
		if (info.metadata == null) return;
		String[] ss = info.metadata.split(":");
		String skeletonAddress = ss[0];
		float yaw = Float.parseFloat(ss[1]);
		museum.getOwner().get
	}


    @Override
    public void show(User user) {



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
