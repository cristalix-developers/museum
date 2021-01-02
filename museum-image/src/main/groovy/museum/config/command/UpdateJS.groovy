@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command


import museum.util.SendScriptUtil
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