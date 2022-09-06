package museum;

import lombok.RequiredArgsConstructor;
import lombok.val;
import museum.client.ClientSocket;
import museum.museum.Coin;
import museum.museum.Museum;
import museum.museum.subject.*;
import museum.player.PlayerDataManager;
import museum.player.User;
import museum.ticker.Ticked;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author func 30.06.2020
 * @project museum
 */
@RequiredArgsConstructor
public class TickTimerHandler extends BukkitRunnable {

	private final App app;
	private final List<Ticked> ticked;
	private final ClientSocket clientSocket;
	// Осторожно, если оно будет маленьким, счетчик не дойдет до Incomeble
	private final static long AUTO_SAVE_PERIOD = 20 * 60L * 10;
	private final PlayerDataManager dataManager;
	private final AtomicInteger counter = new AtomicInteger(1);

	@Override
	public void run() {
		savePlayers();

		// Вызов обработки тиков у всех побочных обработчиков
		for (Ticked tickUnit : ticked)
			tickUnit.tick(counter.get());

		long currentTime = System.currentTimeMillis();
		for (Player player : Bukkit.getOnlinePlayers()) {
			val user = app.getUser(player.getUniqueId());
			if (user == null)
				continue;
			if (user.getMuseums().size() == 0)
				continue;
			for (Museum museum : user.getMuseums()) {
				process(museum, user, currentTime);
			}
		}
	}

	/**
	 * Переодическое автосохранение всех игроков
	 */
	private void savePlayers() {
		val now = counter.get();
		if (now % AUTO_SAVE_PERIOD == 0) {
			counter.set(1);
			clientSocket.write(dataManager.bulk(false));
		} else
			counter.incrementAndGet();
	}

	private void process(Museum museum, User user, long currentTime) {
		for (Subject subject : museum.getSubjects()) {
			val now = counter.get();
			if (subject instanceof CollectorSubject)
				((CollectorSubject) subject).move(currentTime);
			else if (now % 5 == 0 && subject instanceof FountainSubject)
				((FountainSubject) subject).throwWater();
			else if (now % 3 == 0 && subject instanceof RelicShowcaseSubject)
				((RelicShowcaseSubject) subject).rotate();
			// Если постройка может приносить доход, попробовать
			if (subject instanceof Incomeble && now % 20 == 0) // else добавлять не нужно
				((Incomeble) subject).handle(now);
		}
		// Если монеты устарели, что бы не копились на клиенте, удаляю
		museum.getCoins().removeIf(coin -> {
			if (coin.getTimestamp() + Coin.SECONDS_LIVE * 1000 < currentTime) {
				coin.remove(user.getConnection());
				return true;
			}
			return false;
		});
	}

}
