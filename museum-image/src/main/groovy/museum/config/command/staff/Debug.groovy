@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.staff

import museum.App
import museum.multi_chat.ChatType
import museum.multi_chat.MultiChatUtil
import museum.util.MessageUtil
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService

def app = App.app

registerCommand 'debug' handle {
    def user = app.getUser(player.uniqueId)
    def isStaffPlayer = IPermissionService.get().isStaffMember(player.uniqueId)

    if ((user.prefix && user.prefix.contains('㗒')) || player.op || isStaffPlayer) {
        user.debug = !user.debug
        MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.fine("§bРежим отладки §c" + MessageUtil.getFormattedState(user.debug)))
    }
}