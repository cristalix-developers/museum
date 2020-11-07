package museum.excavation;

import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.player.User;
import museum.util.MessageUtil;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author func 07.11.2020
 * @project museum
 */
@UtilityClass
public class PlacesMechanic {

	private static final List<Place> places = new ArrayList<>();

	public static void init(App app) {
		places.addAll(app.getMap().getLabels("place").stream()
				.map(place -> {
					val tag = place.getTag();
					val ss = tag.split("\\s+");
					return new Place(
							UtilV3.fromVector(place.toVector()),
							Double.parseDouble(ss[0]),
							Integer.parseInt(ss[1]),
							tag.substring(2 + ss[0].length() + ss[1].length())
					);
				}).collect(Collectors.toList())
		);
	}

	public static void handleMove(User user, V3 to) {
		for (Place place : places) {
			if (user.getClaimedPlaces().contains(place.getTitle()))
				continue;
			val v3 = place.getPlace();
			if (v3.distanceSquared(to) < place.getRadius() * place.getRadius()) {
				user.giveExperience(place.getClaimedExp());
				user.getClaimedPlaces().add(place.getTitle());
				MessageUtil.find("claim-place")
						.set("title", place.getTitle())
						.set("exp", place.getClaimedExp())
						.send(user);
				user.getPlayer().sendTitle("Найдено место!", "§b+" + place.getClaimedExp() + "EXP");
			}
		}
	}

	@Data
	static class Place {
		private final V3 place;
		private final double radius;
		private final int claimedExp;
		private final String title;
	}
}