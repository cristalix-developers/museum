@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.staff

import museum.App
import museum.misc.Relic
import org.bukkit.Bukkit
import ru.cristalix.core.formatting.Formatting

registerCommand 'relic' handle {
    if (player.op) {
        try {
            if (args.length > 1)
                new Relic(args[1]).give(App.app.getUser(Bukkit.getPlayer(args[0])))
            else
                new Relic(args[0]).give(App.app.getUser(player))
            return Formatting.fine("Реликвия выдана")
        } catch (Exception e) {
            return e.message
        }
    }
}
