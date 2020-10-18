@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.util.MessageUtil
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer

registerCommand 'congr' handle {
    if (args.length != 1)
        return
    def victim = Bukkit.getPlayer(args[0]) as CraftPlayer
    if (victim && victim.isOnline()) {
        MessageUtil.find('congrats')
            .set('sender', player.name)
            .send App.app.getUser(victim)
    }
}
