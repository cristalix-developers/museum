@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.admin

import museum.App
import museum.multi_chat.ChatType
import museum.multi_chat.MultiChatUtil
import museum.packages.SaveUserPackage
import museum.packages.UserInfoPackage
import ru.cristalix.core.formatting.Formatting

import java.util.concurrent.TimeUnit

def app = App.app

registerCommand 'opstat' handle {
    try {
        if (player.op) {
            MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.fine("Начало работы."))

            def uuid = UUID.fromString(args[1])
            def value = args[2].toInteger()
            def data = app.clientSocket.writeAndAwaitResponse(new UserInfoPackage(uuid)).get(5L, TimeUnit.SECONDS)

            MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.fine("Данные игрока успешно получены."))
            if (args[0] == "money") data.userInfo.setMoney(value)
            else if (args[0] == "exp") data.userInfo.setExperience(value)

            MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.fine("Данные игрока изменены."))

            app.clientSocket.write(new SaveUserPackage(uuid, data.userInfo))

            MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.fine("Новые данные игрока сохранены."))
        }
    } catch (Exception ignored) {
        MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.error("Команда пишется так: /opstat <money/exp> <uuid> <значение>"))
    }
}