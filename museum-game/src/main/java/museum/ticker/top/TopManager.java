package museum.ticker.top;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import museum.App;
import museum.data.UserInfo;
import museum.packages.TopPackage;
import museum.ticker.Ticked;
import museum.tops.TopEntry;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;

/**
 * @author func 25.09.2020
 * @project museum
 */
@RequiredArgsConstructor
public class TopManager implements Ticked {

	private static final int UPDATE_SECONDS = 20;
	private static final int DATA_COUNT = 10;
	private final App app;
	private final Map<TopPackage.TopType, List<TopEntry<UserInfo, Object>>> tops = Maps.newConcurrentMap();

	@Override
	public void tick(int... args) {
		if (args[0] % (20 * UPDATE_SECONDS) == 0) {
			updateData();
			sendTops();
		}
	}

	public void updateData() {
		for (TopPackage.TopType type : TopPackage.TopType.values()) {
			app.getClientSocket().writeAndAwaitResponse(new TopPackage(type, DATA_COUNT))
					.thenAcceptAsync(topPackage -> {
						if (tops.containsKey(type)) tops.replace(type, topPackage.getEntries());
						else tops.put(type, topPackage.getEntries());
					});
		}
	}

	public void sendTops() {
		// todo: реализовать отправку игрокам топов
	}
}
