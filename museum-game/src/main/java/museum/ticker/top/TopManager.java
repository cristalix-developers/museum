package museum.ticker.top;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.val;
import museum.App;
import museum.client_conversation.ClientPacket;
import museum.packages.TopPackage;
import museum.player.User;
import museum.ticker.Ticked;
import museum.tops.TopEntry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.cristalix.core.GlobalSerializers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author func 25.09.2020
 * @project museum
 */
@RequiredArgsConstructor
public class TopManager implements Ticked, Listener {

	private static final int UPDATE_SECONDS = 120;
	private static final int DATA_COUNT = 15;
	private final App app;
	private final Map<TopPackage.TopType, List<TopEntry<String, Object>>> tops = Maps.newConcurrentMap();

	private String data;
	private final ClientPacket<String> updatePacket = new ClientPacket<>("top-update");

	@Override
	public void tick(int... args) {
		if (args[0] % (20 * UPDATE_SECONDS) == 0) {
			updateData();
			data = GlobalSerializers.toJson(tops);
		}
		if ("{}".equals(data) || data == null || data.isEmpty())
			return;
		val time = System.currentTimeMillis();
		for (User user : app.getUsers()) {
			if (user.getLastTopUpdateTime() == 0) continue;
			if (user.getConnection() != null && time - user.getLastTopUpdateTime() > UPDATE_SECONDS * 1000) {
				user.setLastTopUpdateTime(time);
				updatePacket.send(user, data);
			}
		}
	}

	public void updateData() {
		for (TopPackage.TopType type : TopPackage.TopType.values()) {
			app.getClientSocket().writeAndAwaitResponse(new TopPackage(type, DATA_COUNT))
					.thenAcceptAsync(pkg -> tops.put(type, pkg.getEntries().stream()
							.map(entry -> new TopEntry<>(
									entry.getDisplayName(),
									entry.getValue()
							)).collect(Collectors.toList())
					));
		}
	}
}