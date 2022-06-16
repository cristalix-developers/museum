@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.staff

import museum.App
import museum.util.MessageUtil
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService

def app = App.app

registerCommand 'fly' handle {
    def user = app.getUser(player.uniqueId)
    def isStaffPlayer = IPermissionService.get().isStaffMember(player.uniqueId)

    if ((user.prefix && user.prefix.contains('㗒')) || player.op || isStaffPlayer) {
        if (!user.player.flying) {
            user.player.allowFlight = true
            user.player.flying = true
        } else {
            user.player.allowFlight = false
            user.player.flying = false
        }
        player.sendMessage(Formatting.fine("§bПолёт §c" + MessageUtil.getFormattedState(user.player.flying)))
    }
}