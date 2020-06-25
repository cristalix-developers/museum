package ru.cristalix.museum.gallery;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.util.WarpUtil;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author func 25.06.2020
 * @project museum
 */
@Getter
@RequiredArgsConstructor
public class Warp {

	private final String address;
	private final Location start;
	private final Location finish;
	@Setter
	private Predicate<User> condition;
	@Setter
	private Consumer<User> after;
	@Setter
	private Consumer<User> before;

	public void warp(User user) {
		if (condition != null && !condition.test(user))
			return;
		Consumer<User> body = usr -> {
			Player player = usr.getPlayer();
			if (usr.getLastWarp() != null && usr.getLastWarp().equals(this)) {
				if (start != null)
					player.teleport(start);
				else
					player.teleport(WarpUtil.get(user.getCurrentMuseum().getPrototype().getAddress()).getFinish());
			} else
				player.teleport(finish);
			usr.setLastWarp(this);
		};

		if (before != null)
			body = before.andThen(body);
		if (after != null)
			body = body.andThen(after);

		body.accept(user);
	}
}
