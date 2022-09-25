@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.client_conversation.AnimationUtil
import museum.config.gui.PrefixBox

registerCommand 'prefixbox' handle {
    def user = App.app.getUser player
    if (user.money > 100000000) {
        user.giveMoney(-100000000)
        PrefixBox.givePrefix(user)
    } else {
        AnimationUtil.buyFailure(user)
    }
}
