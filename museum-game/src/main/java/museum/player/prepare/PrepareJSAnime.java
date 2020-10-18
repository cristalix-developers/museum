package museum.player.prepare;

import museum.App;
import museum.player.User;
import museum.util.SendScriptUtil;
import org.bukkit.Bukkit;
import ru.cristalix.core.display.messages.JavaScriptMessage;

import java.io.File;
import java.util.stream.Stream;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareJSAnime implements Prepare {

	public static final Prepare INSTANCE = new PrepareJSAnime();
	private static final String SCRIPT_PATH = "scripts/";
	private JavaScriptMessage[] codes;

	public PrepareJSAnime() {
		File dir = new File(SCRIPT_PATH);
		dir.mkdirs();
		File[] files = dir.listFiles();

		if (files == null) {
			Bukkit.getLogger().severe("Cannot load scripts at " + SCRIPT_PATH + "!");
			return;
		}
		this.codes = Stream.of(files)
				.map(file -> new JavaScriptMessage(file.list()))
				.toArray(JavaScriptMessage[]::new);
	}

	@Override
	public void execute(User user, App app) {
		SendScriptUtil.sendScripts(user.getPlayer(), codes);
	}
}