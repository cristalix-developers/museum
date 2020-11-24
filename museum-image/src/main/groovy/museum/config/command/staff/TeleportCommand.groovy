@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.staff

import clepto.bukkit.command.CommandContext
import clepto.cristalix.Cristalix
import museum.App
import org.bukkit.Bukkit
import ru.cristalix.core.realm.RealmId

static def getIfLS(CommandContext context) {
    if (context.args.length == 0)
        return null
    def user = App.app.getUser(context.player.uniqueId)
    if (user.prefix.contains 'LS')
        return user
    return null
}

registerCommand 'tpshow' handle {
    def user = getIfLS(delegate)
    if (user) {
        def player = Bukkit.getPlayer args[0]
        if (player && player.online) {
            user.player.showPlayer App.app, player
            user.teleport player.location
        }
    }
}

registerCommand 'tpmus' handle {
    if (getIfLS(delegate)) {
        Cristalix.transfer([player.uniqueId], RealmId.of('MUSM-' + args[0]))
    }
}