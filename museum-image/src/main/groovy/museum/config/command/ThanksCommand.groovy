@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.multi_chat.ChatType
import museum.multi_chat.MultiChatUtil
import museum.packages.ThanksExecutePackage
import ru.cristalix.core.formatting.Formatting

registerCommand 'thx' handle {
    App.app.clientSocket.writeAndAwaitResponse(new ThanksExecutePackage(player.uniqueId)).thenAccept {
        def user = App.app.getUser player
        if (App.app.playerDataManager.getBoosterCount() == 0)  {
            MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.error('Нету активных бустеров 㬫'))
            return
        } else if (it.boostersCount == 0) {
            MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.error('Вы уже всех поблагодарили 㬫'))
            return
        }
        MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.fine("Вы поблагодарили за $it.boostersCount бустер(ов)! + Монеты 㳞"))
        user.giveMoney user.income * it.boostersCount
    }
    return
}
