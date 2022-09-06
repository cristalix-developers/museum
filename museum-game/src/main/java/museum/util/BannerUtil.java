package museum.util;

import lombok.val;
import me.func.mod.Banners;
import me.func.protocol.element.Banner;
import me.func.protocol.element.MotionType;
import museum.museum.subject.Subject;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.UUID;

public class BannerUtil {
    public static void createBanners(Subject subject) {
        val alloc = subject.getAllocation();
        val origin = alloc.getOrigin();
        val world = origin.world;
        val faces = Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);
        for (val face : faces) {
            Location vec = new Location(world, 0, 0, 0);
            double offset = 0.52;
            double yaw = 0.0;
            switch (face) {
                case EAST:
                    vec = new Location(world, offset, .0, .0);
                    yaw = -90.0;
                    break;
                case WEST:
                    vec = new Location(world, -offset, .0, .0);
                    yaw = 90.0;
                    break;
                case SOUTH:
                    vec = new Location(world, .0, .0, offset);
                    yaw = 0.0;
                    break;
                case NORTH:
                    vec = new Location(world, .0, .0, -offset);
                    yaw = 180.0;
                    break;
            }
            val banner = new Banner.Builder()
                    .motionType(MotionType.CONSTANT)
                    .watchingOnPlayer(false)
                    .yaw((float) yaw)
                    .pitch(0.0f)
                    .content("\n§6Доход: §f\n" + String.format("%.2f", subject.getIncome()))
                    .resizeLine(0, 0.3)
                    .resizeLine(1, 0.3)
                    .resizeLine(2, 0.3)
                    .height(16)
                    .weight(16)
                    .x(origin.toCenterLocation().x + vec.x + face.modX * Math.abs(origin.x - alloc.getMin().getX()))
                    .y(origin.y + 1)
                    .z(origin.toCenterLocation().z + vec.z + face.modZ * Math.abs(origin.z - alloc.getMin().getZ()))
                    .red(0)
                    .green(0)
                    .blue(0)
                    .opacity(0.83)
                    .build();
            subject.getBannerUUIDs().add(banner.getUuid());
            Banners.add(banner);
            Banners.show(subject.getOwner().getPlayer(), banner);
        }
    }

    public static void deleteBanners(Subject subject) {
        for (UUID uuid : subject.getBannerUUIDs()) {
            Banners.hide(subject.getOwner().getPlayer(), uuid);
            Banners.remove(uuid);
        }
        subject.getBannerUUIDs().clear();
    }

    public static void updateBanners(Subject subject) {
        deleteBanners(subject);
        createBanners(subject);
    }
}
