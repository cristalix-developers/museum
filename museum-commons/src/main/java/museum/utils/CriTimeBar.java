package museum.utils;

import lombok.Getter;
import lombok.Setter;
import ru.cristalix.core.display.IDisplayService;
import ru.cristalix.core.display.enums.EnumPosition;
import ru.cristalix.core.display.enums.EnumUpdateType;
import ru.cristalix.core.display.messages.ProgressMessage;
import ru.cristalix.core.formatting.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CriTimeBar {

	private final List<UUID> loop = new ArrayList<>();

	private EnumPosition enumPosition;
	private float percent;
	private String title;
	private Color color;

	public CriTimeBar(EnumPosition position, String title, float percent, Color color) {
		this.enumPosition = position;
		this.percent = percent;
		this.title = title;
		this.color = color;
	}

	public void add(UUID uid) {
		loop.add(uid);
		send(uid, build(EnumUpdateType.ADD));
	}

	public void remove(UUID uid) {
		if (loop.remove(uid))
			send(uid, build(EnumUpdateType.REMOVE));
	}

	public void update() {
		sendLoop(build(EnumUpdateType.UPDATE));
	}

	private void sendLoop(ProgressMessage message) {
		IDisplayService.get().sendProgress(loop, message);
	}

	private void send(UUID uid, ProgressMessage message) {
		IDisplayService.get().sendProgress(uid, message);
	}

	private ProgressMessage build(EnumUpdateType type) {
		return ProgressMessage.builder().updateType(type).position(enumPosition).color(color).percent(percent).name(title).build();
	}

}
