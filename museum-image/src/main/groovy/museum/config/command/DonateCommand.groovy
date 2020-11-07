@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import clepto.bukkit.menu.Guis
import museum.App
import museum.data.PickaxeType
import museum.data.SubjectInfo
import museum.donate.DonateType
import museum.museum.Museum
import museum.museum.subject.CollectorSubject
import museum.packages.SaveUserPackage
import museum.prototype.Managers
import museum.util.SubjectLogoUtil
import ru.cristalix.core.formatting.Formatting

registerCommand 'donate' handle {
    Guis.open player, 'donate', player
}

registerCommand 'proccessdonate' handle {
    def user = App.app.getUser player
    user.closeInventory()
    def donate
    try {
        donate = DonateType.valueOf(args[0]) as DonateType
    } catch (Exception ignored) {
        return
    }

    App.app.processDonate(user.getUuid(), donate).thenAccept(error -> {
        if (error) {
            user.sendMessage(Formatting.error(error))
            return
        }
        if (donate == DonateType.LEGENDARY_PICKAXE) {
            user.pickaxeType = PickaxeType.LEGENDARY
            user.donates.add(donate as DonateType)
            user.sendMessage(Formatting.fine("Вы купили §bлегендарную кирку§f! Спасибо за поддержку режима. 㶅"))
        } else if (donate == DonateType.STEAM_PUNK_COLLECTOR) {
            def subject = new CollectorSubject(
                    Managers.subject.getPrototype('punk-collector'),
                    new SubjectInfo(UUID.randomUUID(), 'punk-collector'),
                    user
            )
            user.getSubjects().add(subject)
            user.sendMessage(Formatting.fine("Вы купили §6стип-панк сборщик монет§f! Спасибо за поддержку режима. 㶅"))
            if (user.state instanceof Museum) {
                user.getInventory().addItem SubjectLogoUtil.encodeSubjectToItemStack(subject)
            } else {
                user.sendMessage(Formatting.fine("Что бы получить его, перейдите в музей."))
            }
            user.donates.add(donate as DonateType)
        } /*else if (donate == DonateType.LOCAL_MONEY_BOOSTER) {
            user.addLocalBooster(BoosterType.COINS)
            user.sendMessage(Formatting.fine("Вы купили §6локальный бустер денег§f! Спасибо за поддержку режима. 㶅"))
        } else if (donate == DonateType.LOCAL_EXP_BOOSTER) {
            user.addLocalBooster(BoosterType.EXP)
            user.sendMessage(Formatting.fine("Вы купили §6локальный бустер опыта§f! Спасибо за поддержку режима. 㶅"))
        }*/
        App.app.clientSocket.write(new SaveUserPackage(user.getUuid(), user.generateUserInfo()))
    })
    return
}
