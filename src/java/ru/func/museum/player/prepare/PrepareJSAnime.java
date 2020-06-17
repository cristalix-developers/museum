package ru.func.museum.player.prepare;

import org.bukkit.entity.Player;
import ru.cristalix.core.display.IDisplayService;
import ru.cristalix.core.display.messages.JavaScriptMessage;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareJSAnime implements Prepare {

    private JavaScriptMessage code;
    private static final String FILENAME = "anime_min.js";

    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        if (code == null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(app.getResource(FILENAME)))) {
                code = new JavaScriptMessage(new String[]{reader.lines().collect(Collectors.joining("\n"))});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        IDisplayService.get().sendScripts(player.getUniqueId(), code);
    }
}
