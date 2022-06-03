@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.client_conversation.AnimationUtil
import museum.config.gui.LootBox

registerCommand 'lootboxopen' handle {
    def user = App.app.getUser player
    if (user.money > 10000000) {
        user.giveMoney(-10000000)
        LootBox.giveDrop(user)
    } else {
        AnimationUtil.buyFailure(user)
    }
}