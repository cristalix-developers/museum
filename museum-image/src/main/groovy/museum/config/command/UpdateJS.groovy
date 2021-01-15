@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.client_conversation.ScriptTransfer
import museum.util.SendScriptUtil
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import ru.cristalix.core.display.messages.JavaScriptMessage

registerCommand 'u' handle {
    if (!sender.op) return

    def fileName = args.length > 0 ? args[0] : "js"
    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\") || fileName.contains(":"))
        return "&cНедопустимое имя файла."
    // Потенциальная дыра в безопасности
    def file = new File("scripts/" + fileName + ".bundle.js")
    SendScriptUtil.sendScripts(player.uniqueId, new JavaScriptMessage(file.text))
    return "&bСкрипт объемом &f&l${file.bytes.size()}&b байт был отправлен. &f㲙"
}

registerCommand 'pm' handle {
    if (player.op) {
        if (args.length < 3)
            return "&cИспользование: &e/pm [item/str/int] [Канал] [Сообщение]"
        if (args[0] == 'item') {
            new ScriptTransfer().item(CraftItemStack.asNMSCopy(player.inventory.itemInHand)).send(args[1], App.app.getUser(player))
        } else if (args[0] == 'str') {
            new ScriptTransfer().string(args.drop(2).join(' ')).send(args[1], App.app.getUser(player))
        } else if (args[0] == 'int') {
            new ScriptTransfer().integer(args.drop(2).join(' ') as Integer).send(args[1], App.app.getUser(player))
        }
        return "&bСообщение было отправлено. &f㜗"
    }
    return null
}

/*
registerCommand 'end' handle {
    new ScriptTransfer()
            .item(CraftItemStack.asNMSCopy(new ItemStack(IRON_SWORD)))
            .item(CraftItemStack.asNMSCopy(new ItemStack(BOW)))
            .send('csc:tab-create', App.app.getUser(player))
}*/
