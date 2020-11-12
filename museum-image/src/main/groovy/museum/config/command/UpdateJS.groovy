@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.client_conversation.ClientPacket
import museum.player.prepare.PrepareJSAnime
import museum.util.SendScriptUtil
import ru.cristalix.core.display.messages.JavaScriptMessage

registerCommand 'u' handle {
    def fileName = args.length > 0 ? args[0] : "js"
    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\") || fileName.contains(":"))
        return "&cНедопустимое имя файла."
    // Потенциальная дыра в безопасности
    if (PrepareJSAnime.AVAILABLE_SCRIPTS.contains(fileName) || player.op) {
        def file = new File("scripts/" + fileName + ".bundle.js")
        SendScriptUtil.sendScripts(player.uniqueId, new JavaScriptMessage(file.text))
        if (player.op) {
            return "&bСкрипт объемом &f&l${file.bytes.size()}&b байт был отправлен. &f㲙"
        }
    }
}

registerCommand 'pm' handle {
    if (player.op) {
        if (args.length < 1)
            return "&cИспользование: &e/pm [Канал (по умолчанию 'channel')] [Сообщение]"
        def dataLength = 0
        if (args.length == 1) {
            dataLength = args[0].bytes.length
            new ClientPacket<String>('disable').send App.app.getUser(player), args[0]
        } else if (args.length >= 2) {
            def data = args.drop(1).join(' ')
            dataLength = data.bytes.length
            new ClientPacket<String>(args[0]).send App.app.getUser(player), data
        }
        return "&bСообщение объемом &f&l$dataLength&b байт было отправлено. &f㜗"
    }
    return null
}