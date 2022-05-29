@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import me.func.mod.Anime
import museum.App
import museum.util.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer

registerCommand 'congr' handle {
    if (args.length != 1 || !(sender instanceof CraftPlayer))
        return
    if (sender.name.toLowerCase().contains("нигер") || sender.name.toLowerCase().contains("пидор"))
        return
    def victim = Bukkit.getPlayer(args[0])
    if (victim && victim.isOnline() && App.getApp().getUser(victim).messages) {
        if (victim == sender)
            MessageUtil.find 'lonely' send App.app.getUser(sender as CraftPlayer)
        else {
            MessageUtil.find 'congr-send' send App.app.getUser(sender as CraftPlayer)
            Anime.cursorMessage(victim, sender.player.name + ' ' + (Math.random() > 0.5f ? '䂋' : '㜑'))
        }
    }
}

registerCommand 'con' handle {
    if (sender instanceof CraftPlayer) {
        def user = App.app.getUser(sender as CraftPlayer)
        user.messages = !user.messages
    }
}
