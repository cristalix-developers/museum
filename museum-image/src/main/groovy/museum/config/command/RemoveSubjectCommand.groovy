@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.multi_chat.ChatType
import museum.multi_chat.MultiChatUtil
import museum.museum.Museum
import museum.util.SubjectLogoUtil
import ru.cristalix.core.formatting.Formatting

registerCommand 'remove' handle {
    def user = App.app.getUser(player)
    def state = user.state

    if (state instanceof Museum) {
        def subject = SubjectLogoUtil.decodeItemStackToSubject(user, user.getInventory().getItemInHand())
        if (subject) {
            user.getInventory().setItemInHand(null)
            user.subjects.remove(subject)
            MultiChatUtil.sendMessage(user.getPlayer(), ChatType.SYSTEM, Formatting.fine("Постройка успешно удалена."))
        }
    }
}

