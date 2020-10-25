@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import clepto.bukkit.menu.Guis
import museum.App
import museum.data.PickaxeType
import museum.data.SubjectInfo
import museum.donate.DonateType
import museum.museum.subject.CollectorSubject
import museum.prototype.Managers

registerCommand 'donate' handle {
    Guis.open player, 'donate', player
}

registerCommand 'proccessdonate' handle {
    def user = App.app.getUser player

    user.sendMessage("Я тебе не позволю донатить на тестовом сервере!")
    return

    def donate
    try {
        donate = DonateType.valueOf(args[0]) as DonateType
    } catch (Exception ignored) {
        return
    }

    if (donate == DonateType.LEGENDARY_PICKAXE && user.pickaxeType == PickaxeType.LEGENDARY)
        return

    App.app.processDonate(user.getUuid(), donate).thenAccept(transaction -> {
        if (!transaction.ok)
            return
        if (donate == DonateType.LEGENDARY_PICKAXE) {
            user.pickaxeType = PickaxeType.LEGENDARY
            user.sendMessage("§bПолучена легендарная кирка!")
        } else if (donate == DonateType.STEAM_PUNK_COLLECTOR) {
            user.getSubjects().add(new CollectorSubject(
                    Managers.subject.getPrototype('punk-collector'),
                    new SubjectInfo(UUID.randomUUID(), 'punk-collector'),
                    user
            ))
            user.sendMessage("§bПолучен стим-панк коллектор!")
        }
    })
}
