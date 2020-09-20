package museum;

import lombok.RequiredArgsConstructor;
import lombok.val;
import museum.museum.Museum;
import museum.museum.subject.Subject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import museum.client.ClientSocket;
import museum.museum.Coin;
import museum.museum.map.SubjectType;
import museum.museum.subject.CollectorSubject;
import museum.player.PlayerDataManager;
import museum.ticker.Ticked;

import java.util.List;

/**
 * @author func 30.06.2020
 * @project museum
 */
@RequiredArgsConstructor
public class TickTimerHandler extends BukkitRunnable {

	private final App app;
	private final List<Ticked> ticked;
	private final ClientSocket clientSocket;
	private final static long AUTO_SAVE_PERIOD = 20 * 60 * 3;
	private final PlayerDataManager dataManager;
	private int counter = 1;

	@Override
	public void run() {
		// Переодическое автосохранение всех игроков
		if (counter % AUTO_SAVE_PERIOD == 0) {
			counter = 1;
			clientSocket.write(dataManager.bulk(false));
		} else
			counter++;

		long time = System.currentTimeMillis();

		// Вызов обработки тиков у всех побочных обработчиков
		for (Ticked ticked : ticked)
			ticked.tick(counter);

		for (Player player : Bukkit.getOnlinePlayers()) {
			val user = app.getUser(player.getUniqueId());

			for (Museum museum : user.getMuseums()) {
				for (Subject subject : museum.getSubjects()) {
					if (subject instanceof CollectorSubject) ((CollectorSubject) subject).move(time);
				}
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
