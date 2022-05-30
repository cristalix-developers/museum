@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import me.func.mod.Anime
import museum.App
import museum.client_conversation.AnimationUtil
import museum.config.gui.LootBox
import museum.config.gui.PrefixBox
import museum.data.PickaxeType
import museum.data.SubjectInfo
import museum.donate.DonateType
import museum.museum.Museum
import museum.museum.subject.CollectorSubject
import museum.packages.SaveUserPackage
import museum.packages.UserTransactionPackage.TransactionResponse
import museum.prototype.Managers
import museum.util.SubjectLogoUtil
import ru.cristalix.core.formatting.Formatting

registerCommand 'proccessdonate' handle {
    def user = App.app.getUser player
    user.closeInventory()
    def donate
    try {
        donate = DonateType.valueOf(args[0]) as DonateType
    } catch (Exception ignored) {
        return ignored.message
    }

    if (App.app.playerDataManager.getBoosterCount() > 5 && donate.name().contains("BOOSTER")) {
        player.sendMessage(Formatting.error("Запущено слишком много бустеров! Подождите пожалуйста..."))
        return
    }

    App.app.processDonate(user.getUuid(), donate).thenAccept(transaction -> {
        if (!transaction.ok) {
            if (transaction == TransactionResponse.INSUFFICIENT_FUNDS)
                AnimationUtil.buyFailure(user)
            user.sendMessage(Formatting.error(transaction.name))
            return
        }
        if (donate == DonateType.ITEM_CASE) {
            LootBox.giveDrop(user)
        } else if (donate == DonateType.PREFIX_CASE) {
            PrefixBox.givePrefix(user)
        } else if (donate == DonateType.PRIVILEGES) {
            user.privileges = true
            user.donates.add(donate as DonateType)
            Anime.topMessage user.handle(), "Вы избавились от комиссии! Спасибо за поддержку. 㶅"
        } else if (donate == DonateType.LEGENDARY_PICKAXE) {
            user.pickaxeType = PickaxeType.LEGENDARY
            user.donates.add(donate as DonateType)
            Anime.topMessage user.handle(), "Вы купили §bлегендарную кирку§f! Спасибо за поддержку. 㶅"
        } else if (donate == DonateType.STEAM_PUNK_COLLECTOR) {
            def subject = new CollectorSubject(
                    Managers.subject.getPrototype('punk-collector'),
                    new SubjectInfo(UUID.randomUUID(), 'punk-collector'),
                    user
            )
            user.getSubjects().add(subject)
            Anime.topMessage user.handle(), "Вы купили §6сборщик монет§f! Спасибо за поддержку. 㶅"
            if (user.state instanceof Museum) {
                user.getInventory().addItem SubjectLogoUtil.encodeSubjectToItemStack(subject)
            } else {
                user.sendMessage(Formatting.fine("Что бы получить его, перейдите в музей."))
            }
            user.donates.add(donate as DonateType)
        }
        App.app.clientSocket.write(new SaveUserPackage(user.getUuid(), user.generateUserInfo()))
    })
    return
}
