@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App

registerCommand 'changeprefix' handle {
    def user = App.app.getUser(player)
    if (args.length < 1 || user.prefix.contains('㗒'))
        return
    for (def prefix in user.info.prefixes) {
        if (prefix.contains(args[0])) {
            user.setPrefix(args[0])
            user.closeInventory()
        }
    }
}