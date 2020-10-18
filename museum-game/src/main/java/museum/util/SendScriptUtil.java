package museum.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.entity.Player;
import ru.cristalix.core.display.IDisplayService;
import ru.cristalix.core.display.messages.JavaScriptMessage;

/**
 * @author func 17.10.2020
 * @project museum
 */
@UtilityClass
public class SendScriptUtil {
	public static void sendScripts(Player player, JavaScriptMessage... code) {
		for (val script : code) {
			IDisplayService.get().sendScripts(player.getUniqueId(), script);
			System.out.println("sending...");
		}
	}
}
