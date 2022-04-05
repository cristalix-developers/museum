@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.staff

import museum.App
import museum.packages.SaveUserPackage
import museum.packages.UserInfoPackage
import ru.cristalix.core.formatting.Formatting

import java.util.concurrent.TimeUnit

def app = App.app

registerCommand 'opstat' handle {
    try {
        if (player.op) {
            player.sendMessage(Formatting.fine("Начало работы."))

            def uuid = UUID.fromString(args[1])
            def value = args[2].toInteger()
            def data = app.clientSocket.writeAndAwaitResponse(new UserInfoPackage(uuid)).get(5L, TimeUnit.SECONDS)

            player.sendMessage(Formatting.fine("Данные игрока успешно получены."))

            if (args[0] == "money") data.userInfo.setMoney(value)
            else if (args[0] == "exp") data.userInfo.setExperience(value)

            player.sendMessage(Formatting.fine("Данные игрока изменены."))

            app.clientSocket.write(new SaveUserPackage(uuid, data.userInfo))

            player.sendMessage(Formatting.fine("Новые данные игрока сохранены."))
        }
    } catch (Exception ignored) {
        player.sendMessage(Formatting.error("Команда пишется так: /opstat <money/exp> <uuid> <значение>"))
    }
}