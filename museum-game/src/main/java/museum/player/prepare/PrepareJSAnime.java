package museum.player.prepare;

import clepto.cristalix.Scripts;
import museum.App;
import museum.player.User;
import museum.util.SendScriptUtil;
import org.bukkit.Bukkit;
import ru.cristalix.core.display.messages.JavaScriptMessage;

import java.io.File;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareJSAnime implements Prepare {

	private static final String SCRIPT_PATH = "scripts/";
	private JavaScriptMessage codes;

	@Override
	public void execute(User user, App app) {
		if (codes == null) {
			File dir = new File(SCRIPT_PATH);
			dir.mkdirs();
			File[] files = dir.listFiles();

			if (files == null) {
				Bukkit.getLogger().severe("Cannot load scripts at " + SCRIPT_PATH + "!");
				return;
			}
			this.codes = Scripts.loadAndMerge(files);
		}
		SendScriptUtil.sendScripts(user.getPlayer(), codes);
	}
}