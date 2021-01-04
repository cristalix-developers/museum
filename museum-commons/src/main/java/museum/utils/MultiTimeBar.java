package museum.utils;

import ru.cristalix.core.CoreApi;
import ru.cristalix.core.display.enums.EnumPosition;
import ru.cristalix.core.formatting.Color;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class MultiTimeBar {

	private final Supplier<List<MultiBarInstance>> supplier;
	private final Supplier<String> ifEmpty;
	private final CriTimeBar bar;
	private final AtomicInteger currentInt = new AtomicInteger(0);
	private MultiBarInstance current;

	public MultiTimeBar(Supplier<List<MultiBarInstance>> itemList, long reloadTime, TimeUnit reloadTimeUnit, Supplier<String> ifEmpty) {
		this.supplier = itemList;
		this.ifEmpty = ifEmpty;
		refreshCurrent();
		this.bar = new CriTimeBar(EnumPosition.TOPTOP, "null", 0.5f, Color.GREEN);
		refreshTimeBar();
		CoreApi.get().getPlatform().getScheduler().runSyncRepeating(this::refreshTimeBar, 1L, TimeUnit.SECONDS);
		CoreApi.get().getPlatform().getScheduler().runSyncRepeating(() -> {
			refreshCurrent();
			refreshTimeBar();
		}, reloadTime, reloadTimeUnit);
	}

	public void onJoin(UUID user) {
		bar.add(user);
	}

	public void onQuit(UUID user) {
		bar.remove(user);
	}

	private void refreshTimeBar() {
		if (current == null) {
			bar.setTitle(ifEmpty.get());
			bar.setPercent(1);
			bar.update();
			return;
		}
		float chance = (float) (current.getPercentsOfFullTime() / 100);
		if (chance > 1) chance = 1;
		if (chance < 0) chance = 0;
		bar.setPercent(chance);
		bar.setTitle(current.getTitle() );
		bar.setColor(Color.values()[currentInt.get()]);
		bar.update();
	}

	private void refreshCurrent() {
		List<MultiBarInstance> list = supplier.get();
		if (list == null || list.isEmpty()) {
			current = null;
			return;
		}
		int j = currentInt.getAndIncrement();
		if (j >= list.size()) {
			j = 0;
			currentInt.set(1);
		}
		this.current = list.get(j);
	}

	public interface MultiBarInstance {

		double getPercentsOfFullTime();

		String getTitle();

	}

}
