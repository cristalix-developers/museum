@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.player.prepare.BeforePacketHandler
import museum.util.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer

registerCommand 'congr' handle {
    if (args.length != 1 || !(sender instanceof CraftPlayer))
        return
    def victim = Bukkit.getPlayer(args[0])
    if (victim && victim.isOnline()) {
        if (victim == sender)
            MessageUtil.find 'lonely' send App.app.getUser(sender as CraftPlayer)
        else {
            MessageUtil.find 'congr-send' send App.app.getUser(sender as CraftPlayer)
            BeforePacketHandler.DROP_CHANNEL.send(App.app.getUser(victim), sender.player.name + ' ' + (Math.random() > 0.5f ? '䂋' : '㜑'));
        }
    }
}
