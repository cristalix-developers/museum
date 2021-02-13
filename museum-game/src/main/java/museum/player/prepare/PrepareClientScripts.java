package museum.player.prepare;

import clepto.bukkit.B;
import clepto.cristalix.Cristalix;
import museum.App;
import museum.player.User;
import ru.cristalix.core.display.messages.JavaScriptMessage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareClientScripts implements Prepare {

	public static final Prepare INSTANCE = new PrepareClientScripts();

	//private final JavaScriptMessage scriptsMessage;

	public PrepareClientScripts() {
		//try {
		//	InputStream resource = B.plugin.getResource("clientcode.bundle.js");

		//	String jsCode = new BufferedReader(new InputStreamReader(resource))
		//			.lines()
		//			.collect(Collectors.joining("\n"));

		//	scriptsMessage = new JavaScriptMessage(new String[] {jsCode});
		//} catch (Exception exception) {
		//	throw new RuntimeException(exception);
		//}
	}

	@Override
	public void execute(User user, App app) {
		//Cristalix.displayService().sendScripts(user.getUuid(), scriptsMessage);
	}
}