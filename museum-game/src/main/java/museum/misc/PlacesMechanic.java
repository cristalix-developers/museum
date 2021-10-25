package museum.misc;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.fragment.Fragment;
import museum.fragment.Meteorite;
import museum.player.User;
import museum.util.MessageUtil;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author func 07.11.2020
 * @project museum
 */
@UtilityClass
public class PlacesMechanic {

	private static final Map<String, Place> places = Maps.newHashMap();

	public static void init(App app) {
		places.putAll(app.getMap().getLabels("place").stream()
				.map(place -> {
					val tag = place.getTag();
					val ss = tag.split("\\s+");
					return new Place(
							UtilV3.fromVector(place.toVector()),
							Double.parseDouble(ss[0]),
							Integer.parseInt(ss[1]),
							0,
							null,
							tag.substring(2 + ss[0].length() + ss[1].length())
					);
				}).collect(Collectors.toMap(Place::getTitle, place -> place)));
		places.putAll(app.getMap().getLabels("prize").stream()
				.map(place -> {
					val tag = place.getTag();
					val ss = tag.split("\\s+");
					return new Place(
							UtilV3.fromVector(place.toVector()),
							2,
							Integer.parseInt(ss[1]) / 10,
							Integer.parseInt(ss[3]),
							null,
							place.getCoords()
					);
				}).collect(Collectors.toMap(Place::getTitle, place -> place)));
		places.putAll(app.getMap().getLabels("met").stream()
				.map(place -> {
					val meteorite = new Meteorite("meteor_" + place.getTag());
					return new Place(
							UtilV3.fromVector(place.toVector()),
							3,
							10,
							10000,
							meteorite,
							meteorite.getMeteorite().getTitle()
					);
				}).collect(Collectors.toMap(Place::getTitle, place -> place)));
	}

	public static Place getPlaceByTitle(String title) {
		return places.get(title);
	}

	public static void handleMove(User user, V3 to) {
		for (Place place : places.values()) {
			if (user.getClaimedPlaces().contains(place.getTitle()))
				continue;
			val v3 = place.getPlace();
			if (v3.distanceSquared(to) < place.getRadius() * place.getRadius()) {
				user.getClaimedPlaces().add(place.getTitle());
				if (place.claimedMoney > 0) {
					user.setMoney(user.getMoney() + place.claimedMoney);
					AnimationUtil.cursorHighlight(user, "§6+§l" + place.claimedMoney + "$");
				}
				if (place.fragment instanceof Meteorite) {
					place.fragment.give(user);
				}
				user.giveExperience(place.getClaimedExp());
				MessageUtil.find("claim-place")
						.set("title", place.getTitle())
						.set("exp", place.getClaimedExp())
						.send(user);
				user.sendTitle("§7Найдено место!\n\n§b+" + place.getClaimedExp() + "EXP");
				break;
			}
		}
	}

	@Data
	public static class Place {
		private final V3 place;
		private final double radius;
		private final int claimedExp;
		private final int claimedMoney;
		private final Fragment fragment;
		private final String title;
	}
}