@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.admin

import me.func.mod.conversation.ModTransfer
import museum.App
import museum.client_conversation.AnimationUtil
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack

registerCommand 'pm' handle {
    if (player.op) {
        if (args.length < 3)
            return "&cИспользование: &e/pm [item/str/int] [Канал] [Сообщение]"
        if (args[0] == 'item') {
            new ModTransfer().item(CraftItemStack.asNMSCopy(player.inventory.itemInHand)).send(args[1], player)
        } else if (args[0] == 'str') {
            AnimationUtil.generateMessage args.drop(2).join(' '), args[1], App.app.getUser(player)
        } else if (args[0] == 'int') {
            new ModTransfer().integer(args.drop(2).join(' ') as Integer).send(args[1], player)
        }
        return "&bСообщение было отправлено. &f㜗"
    }
    return null
}
