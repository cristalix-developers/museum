package museum.ticker.top;

import clepto.bukkit.B;
import clepto.cristalix.Cristalix;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.val;
import museum.App;
import museum.client_conversation.ClientPacket;
import museum.packages.TopPackage;
import museum.player.User;
import museum.ticker.Ticked;
import museum.tops.TopEntry;
import ru.cristalix.core.GlobalSerializers;
import ru.cristalix.core.account.IAccountService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author func 25.09.2020
 * @project museum
 */
@RequiredArgsConstructor
public class TopManager implements Ticked {

	private static final int UPDATE_SECONDS = 20;
	private static final int DATA_COUNT = 100;
	private final App app;
	private final Map<TopPackage.TopType, List<TopEntry<String, Object>>> tops = Maps.newConcurrentMap();

	@Override
	public void tick(int... args) {
		if (args[0] % (20 * UPDATE_SECONDS) == 0) {
			updateData();
			sendTops();
		}
	}

	public void updateData() {
		Arrays.stream(TopPackage.TopType.values())
				.forEach(type -> app.getClientSocket().writeAndAwaitResponse(new TopPackage(type, DATA_COUNT))
						.thenAcceptAsync(pkg -> tops.put(type, pkg.getEntries().stream()
								.map(entry -> {
									val key = entry.getKey().getUuid();
									val context = Cristalix.permissionService().getPermissionContext(key).join();
									val group = context.getBestGroup();
									val nick = IAccountService.get().getNameByUuid(key).join();
									return new TopEntry<>("" +
											group.getPrefixColor() +
											(group.getPrefix().isEmpty() ? "" : group.getPrefix() + " ") +
											group.getNameColor() +
											(context.getColor() == null ? "" : context.getColor()) +
											nick,
											entry.getValue()
									);
								}).collect(Collectors.toList())
						)));
	}

	public void sendTops() {
		val data = GlobalSerializers.toJson(tops);
		val packet = new ClientPacket<String>("top-update");
		for (User user : app.getUsers())
			packet.send(user, data);
	}
}
