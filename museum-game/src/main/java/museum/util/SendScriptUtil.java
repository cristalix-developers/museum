package museum.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import ru.cristalix.core.display.IDisplayService;
import ru.cristalix.core.display.messages.JavaScriptMessage;

import java.util.UUID;

/**
 * @author func 17.10.2020
 * @project museum
 */
@UtilityClass
public class SendScriptUtil {
	public static void sendScripts(UUID uuid, JavaScriptMessage... code) {
		for (val script : code)
			IDisplayService.get().sendScripts(uuid, script);
	}
}
