@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.admin

import museum.App
import museum.fragment.Gem
import org.bukkit.Bukkit

registerCommand 'gem' handle {
    if (player.op) {
        new Gem(args[1]).give(App.app.getUser(Bukkit.getPlayer(args[0])))
    }
}
