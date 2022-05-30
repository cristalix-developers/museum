@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.staff

import museum.App
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Formatting

def app = App.app

registerCommand 'debug' handle {
    def user = app.getUser(player.uniqueId)
    def real = player as Player

    if ((user.prefix && user.prefix.contains('㗒')) || player.op) {
        user.debug = !user.debug
        real.sendMessage(Formatting.fine("Режим отладки: " + user.debug))
    }
}