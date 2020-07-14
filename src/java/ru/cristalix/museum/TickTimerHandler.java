package ru.cristalix.museum;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.cristalix.museum.client.ClientSocket;
import ru.cristalix.museum.museum.Coin;
import ru.cristalix.museum.museum.map.SubjectType;
import ru.cristalix.museum.museum.subject.CollectorSubject;
import ru.cristalix.museum.player.PlayerDataManager;
import ru.cristalix.museum.visitor.VisitorManager;

/**
 * @author func 30.06.2020
 * @project museum
 */
@RequiredArgsConstructor
public class TickTimerHandler extends BukkitRunnable {

	private final App app;
	private final VisitorManager visitorManager;
	private final ClientSocket clientSocket;
	private final static long AUTO_SAVE_PERIOD = 20 * 60 * 3;
	private final PlayerDataManager dataManager;
	private long counter = 1;

	@Override
	public void run() {
		// Переодическое автосохранение всех игроков
		if (counter % AUTO_SAVE_PERIOD == 0) {
			counter = 1;
			clientSocket.write(dataManager.bulk(false));
		} else
			counter++;

		long time = System.currentTimeMillis();
		val visitedPoint = null; // todo: do after

		for (Player player : Bukkit.getOnlinePlayers()) {
			val user = app.getUser(player.getUniqueId());

			if (user.getExcavation() != null || user.getCurrentMuseum() == null)
				continue;

			if (visitedPoint != null && user.getCoins().size() < 50) {
				Coin coin = new Coin(visitedPoint);
				coin.create(user.getConnection());
				user.getCoins().add(coin);
			}
			for (CollectorSubject collector : user.getCurrentMuseum().getSubjects(SubjectType.COLLECTOR)) {
				collector.move(user, time);
			}

			// Если монеты устарели, что бы не копились на клиенте, удаляю
			user.getCoins().removeIf(coin -> {
				if (coin.getTimestamp() + Coin.SECONDS_LIVE * 1000 < time) {
					coin.remove(user.getConnection());
					return true;
				}
				return false;
			});
		}
	}
}
