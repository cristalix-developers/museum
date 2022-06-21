@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.admin

import museum.App
import museum.museum.subject.SkeletonSubject
import org.bukkit.Bukkit
import ru.cristalix.core.formatting.Formatting

registerCommand 'clearsubjects' handle {
    if (player.op) {
        def victim = App.app.getUser(Bukkit.getPlayer(args[0]))
        victim.getSubjects().stream()
                .filter(subject -> subject instanceof SkeletonSubject)
                .map(subject -> subject as SkeletonSubject)
                .forEach {
                    it.skeleton = null
                    it.updateInfo()
                }
        player.sendMessage(Formatting.fine("Успешно!"))
    }
}
