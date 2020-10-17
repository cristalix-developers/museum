package museum.util;

import clepto.cristalix.Cristalix;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import ru.cristalix.core.display.messages.JavaScriptMessage;

/**
 * @author func 17.10.2020
 * @project museum
 */
@UtilityClass
public class SendScriptUtil {
	public static void sendScripts(Player player, JavaScriptMessage code) {
		Cristalix.displayService().sendScripts(player.getUniqueId(), code);
	}
}
