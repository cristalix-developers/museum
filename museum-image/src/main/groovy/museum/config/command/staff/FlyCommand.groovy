@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.staff

import museum.App

def app = App.app

registerCommand 'fly' handle {
    def user = app.getUser(player.uniqueId)
    if (user.prefix.contains 'LS') {
        user.player.allowFlight = true
        user.player.flying = true
    }
}
