@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.packages.ThanksExecutePackage
import ru.cristalix.core.formatting.Formatting

registerCommand 'thx' handle {
    App.app.clientSocket.writeAndAwaitResponse(new ThanksExecutePackage(player.uniqueId)).thenAccept {
        def user = App.app.getUser player
        if (new RequestGlobalBoostersPackage().boosters.empty)  {
            player.sendMessage(Formatting.error('Нету активных бустеров 㬫'))
            return
        } else if (it.boostersCount == 0) {
            player.sendMessage(Formatting.error('Вы уже всех поблагодарили 㬫'))
            return
        }
        player.sendMessage(Formatting.fine("Вы поблагодарили за $it.boostersCount бустер(ов)! + Монеты 㳞"))
        user.giveMoney user.income * it.boostersCount
    }
    return
}
