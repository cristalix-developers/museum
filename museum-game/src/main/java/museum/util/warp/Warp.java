package museum.util.warp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import museum.prototype.Managers;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import museum.player.User;

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
	@Setter
	private Consumer<User> onBack;
	@Setter
	private Consumer<User> onForward;

	public void warp(User user) {
		if (condition != null && !condition.test(user))
			return;
		final boolean forward = user.getLastWarp() == null || !user.getLastWarp().equals(this);
		Consumer<User> pipeline = usr -> {
			Player player = usr.getPlayer();
			if (forward) {
				player.teleport(finish);
				usr.setLastWarp(this);
			} else {
				if (start != null) {
					player.teleport(start);
					return;
				}
				user.getCurrentMuseum().show(user);
				usr.setLastWarp(null);
			}
		};
		if (before != null)
			pipeline = before.andThen(pipeline);
		if (after != null)
			pipeline = pipeline.andThen(after);
		if (forward && onForward != null)
			pipeline = pipeline.andThen(onForward);
		if (!forward && onBack != null)
			pipeline = pipeline.andThen(onBack);

		pipeline.accept(user);
	}
}
