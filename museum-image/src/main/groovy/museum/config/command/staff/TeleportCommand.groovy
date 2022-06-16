@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.staff

import clepto.bukkit.command.CommandContext
import clepto.cristalix.Cristalix
import museum.App
import org.bukkit.Bukkit
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.realm.RealmId

static def getIfLS(CommandContext context) {
    if (context.args.length == 0)
        return null
    def user = App.app.getUser(context.player.uniqueId)
    def isStaffPlayer = IPermissionService.get().isStaffMember(context.player.uniqueId)
    if ((user.prefix && user.prefix.contains('㗒')) || context.player.op || isStaffPlayer)
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
            user.sendMessage(Formatting.fine("§bТелепортирован к §c" + player.name))
        }
    }
}

registerCommand 'get' handle {
    def user = getIfLS(delegate)
    if (user) {
        def player = Bukkit.getPlayer args[0]
        if (player && player.online) {
            user.player.showPlayer(App.app, player)
            player.teleport user.location
            user.sendMessage(Formatting.fine("§bК вам телепортирован §c" + player.name))
        }
    }
}

registerCommand 'tpmus' handle {
    if (getIfLS(delegate)) {
        Integer num = 1
        try {
            num = Integer.parseInt(args[0])
        } catch (Exception ignored) {
            return null
        }
        Cristalix.transfer([player.uniqueId], RealmId.of('MUSM-' + num))
    }
}