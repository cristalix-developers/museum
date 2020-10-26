@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.packages.ThanksExecutePackage
import ru.cristalix.core.formatting.Formatting

registerCommand 'thx' handle {
    App.app.clientSocket.writeAndAwaitResponse(new ThanksExecutePackage(player.uniqueId)).thenAccept {
        player.sendMessage(Formatting.fine("Вы поблагодарили за $it.boostersCount бустеров!"))
    }
    return
}
