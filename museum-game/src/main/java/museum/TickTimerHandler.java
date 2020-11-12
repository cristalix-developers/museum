package museum;

import lombok.RequiredArgsConstructor;
import lombok.val;
import museum.client.ClientSocket;
import museum.museum.Coin;
import museum.museum.Museum;
import museum.museum.subject.*;
import museum.packages.MuseumMetricsPackage;
import museum.player.PlayerDataManager;
import museum.player.User;
import museum.ticker.Ticked;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.cristalix.core.realm.IRealmService;

import java.util.List;
import java.util.stream.Collectors;

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
	private final static long AUTO_SAVE_PERIOD = 20 * 60L * 3;
	private final PlayerDataManager dataManager;
	private int counter = 1;

	@Override
	public void run() {
		savePlayers();
		if (counter % 50 == 0)
			App.getApp().getClientSocket().write(getFreshMetrics());

		// Вызов обработки тиков у всех побочных обработчиков
		for (Ticked tickUnit : ticked)
			tickUnit.tick(counter);

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
		if (counter % AUTO_SAVE_PERIOD == 0) {
			counter = 1;
			clientSocket.write(dataManager.bulk(false));
		} else
			counter++;
	}

	private void process(Museum museum, User user, long currentTime) {
		for (Subject subject : museum.getSubjects()) {
			if (subject instanceof CollectorSubject)
				((CollectorSubject) subject).move(currentTime);
			else if (counter % 5 == 0 && subject instanceof FountainSubject)
				((FountainSubject) subject).throwWater();
			else if (counter % 5 == 0 && subject instanceof StallSubject)
				((StallSubject) subject).rotateCustomerHead();
			else if (counter % 3 == 0 && subject instanceof RelicShowcaseSubject)
				((RelicShowcaseSubject) subject).rotate();
			// Если постройка может приносить доход, попробовать
			if (subject instanceof Incomeble) // else добавлять не нужно
				((Incomeble) subject).handle(counter);
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

	private MuseumMetricsPackage getFreshMetrics() {
		val tps = Bukkit.getTPS()[1];
		val runtime = Runtime.getRuntime();
		return new MuseumMetricsPackage(
				IRealmService.get().getCurrentRealmInfo().getRealmId().getRealmName(),
				Bukkit.getOnlinePlayers().size(),
				Math.round(tps * 100F) / 100F,
				runtime.freeMemory(),
				runtime.totalMemory(),
				runtime.maxMemory(),
				PacketMetrics.METRICS.entrySet().stream().collect(Collectors.toMap(
						entry -> entry.getKey().getName(),
						entry -> {
							val value = entry.getValue();
							return new MuseumMetricsPackage.PacketMetric(
									value.received.longValue(),
									value.receivedBytes.longValue(),
									value.sent.longValue(),
									value.sentBytes.longValue(),
									value.decompressedBytes.longValue(),
									value.compressedBytes.longValue()
							);
						}
				))
		);
	}
}
