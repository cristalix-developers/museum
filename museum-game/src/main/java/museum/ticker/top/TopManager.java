package museum.ticker.top;

import com.google.common.collect.Maps;
import museum.App;
import museum.packages.TopPackage;
import museum.packages.TopPackage.TopType;
import museum.ticker.Ticked;
import museum.tops.TopEntry;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import ru.cristalix.boards.bukkitapi.Board;
import ru.cristalix.boards.bukkitapi.Boards;
import ru.cristalix.core.GlobalSerializers;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static museum.packages.TopPackage.TopType.*;

/**
 * @author func 25.09.2020
 * @project museum
 */
public class TopManager implements Ticked, Listener {

	private static final int UPDATE_SECONDS = 30;
	private static final int DATA_COUNT = 15;
	private final App app;
	private final Map<TopType, List<TopEntry<String, String>>> tops = Maps.newConcurrentMap();
	private final Map<TopType, Board> boards = Maps.newConcurrentMap();

	private final DecimalFormat TOP_DATA_FORMAT = new DecimalFormat("###,###,###");

	public TopManager(App app) {
		this.app = app;
		boards.put(INCOME, newBoard("Топ по доходу", "Прибыль", 266, -270, -135));
		boards.put(EXPERIENCE, newBoard("Топ по опыту", "Опыт", 261, -278, -90));
		boards.put(MONEY, newBoard("Топ по деньгам", "Валюта", 266, -286, -45));
	}

	private Board newBoard(
			String title, String fieldName,
			double x, double z, float yaw
	) {
		Board board = Boards.newBoard();

		board.addColumn("#", 20);
		board.addColumn("Игрок", 110);
		board.addColumn(fieldName, 60);

		board.setTitle(title);

		board.setLocation(new Location(app.getWorld(), x, 90.3, z, yaw, 0F));

		Boards.addBoard(board);

		return board;
	}

	@Override
	public void tick(int... args) {
		if (args[0] % (20 * UPDATE_SECONDS) == 0) {
			updateData();
			String data = GlobalSerializers.toJson(tops);
			if ("{}".equals(data) || data == null)
				return;
			boards.forEach((type, top) -> {

				top.clearContent();

				int counter = 0;
				for (TopEntry<String, String> topEntry : tops.get(type)) {
					top.addContent(
							UUID.randomUUID(),
							"" + ++counter,
							topEntry.getKey(),
							topEntry.getValue()
					);
				}

				top.updateContent();
			});
		}
	}

	public void updateData() {
		for (TopType type : values()) {
			app.getClientSocket().writeAndAwaitResponse(new TopPackage(type, DATA_COUNT))
					.thenAcceptAsync(pkg -> tops.put(type, pkg.getEntries().stream()
							.map(entry -> new TopEntry<>(
									entry.getDisplayName(),
									TOP_DATA_FORMAT.format(entry.getValue())
							)).collect(Collectors.toList())
					));
		}
	}
}