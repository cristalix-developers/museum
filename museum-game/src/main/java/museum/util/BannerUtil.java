package museum.util;

import lombok.val;
import lombok.var;
import me.func.mod.world.Banners;
import me.func.protocol.data.element.Banner;
import me.func.protocol.data.element.MotionType;
import museum.App;
import museum.museum.subject.*;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

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
			val height = 16;
			var width = 16;
			if (subject instanceof SkeletonSubject || subject instanceof CollectorSubject || subject instanceof RelicShowcaseSubject) {
				width = 48;
			}

			StringBuilder builder = new StringBuilder();
			builder.append("§b").append(subject.getPrototype().getTitle());

			if (subject instanceof SkeletonSubject) {
				val skeleton = ((SkeletonSubject) subject).getSkeleton();
			    val done = skeleton == null ? 0 : skeleton.getUnlockedFragments().size();
			    val all = skeleton == null ? 0 : skeleton.getPrototype().getFragments().size();

                builder.append("\n").append(done).append(" из ").append(all);
			}

			builder.append("\nДоход §a").append(String.format("%.2f", subject.getIncome())).append("§f\uE03F за 5 сек.");

			val banner = new Banner.Builder()
					.motionType(MotionType.CONSTANT)
					.watchingOnPlayer(false)
					.yaw((float) yaw)
					.pitch(0.0f)
					.content(builder.toString())
					.resizeLine(0, (width > 16.0) ? 0.3 : 0.15)
					.resizeLine(1, (width > 16.0) ? 0.3 : 0.15)
					.resizeLine(2, (width > 16.0) ? 0.3 : 0.15)
					.height(height)
					.weight(width)
					.x(origin.toCenterLocation().x + vec.x + face.modX * Math.abs(origin.x - alloc.getMin().getX()))
					.y(origin.y + 1)
					.z(origin.toCenterLocation().z + vec.z + face.modZ * Math.abs(origin.z - alloc.getMin().getZ()))
					.red(0)
					.green(0)
					.blue(0)
					.opacity(0.47)
					.build();
			subject.getBannerUUIDs().add(banner.getUuid());
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

	public static void showBanners(Player player) {
		for (val s : App.getApp().getUser(player).getSubjects()) {
			if (s instanceof RelicShowcaseSubject || s instanceof SkeletonSubject ||
					s instanceof CollectorSubject || s instanceof FountainSubject) {
				if (s.getAllocation() != null)
					BannerUtil.updateBanners(s);
			}
		}
	}

	public static void hideBanners(Player player) {
		for (val s : App.getApp().getUser(player).getSubjects()) {
			if (s.getAllocation() != null) {
				Banners.hide(player, s.getBannerUUIDs().toArray(new UUID[0]));
			}
		}
	}
}
