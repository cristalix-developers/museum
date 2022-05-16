@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import clepto.bukkit.menu.Guis
import implario.ListUtils
import me.func.mod.Anime
import museum.App
import museum.client_conversation.AnimationUtil
import museum.config.gui.LootBox
import museum.data.PickaxeType
import museum.data.SubjectInfo
import museum.donate.DonateType
import museum.museum.Museum
import museum.museum.subject.CollectorSubject
import museum.packages.SaveUserPackage
import museum.packages.UserTransactionPackage.TransactionResponse
import museum.player.User
import museum.prototype.Managers
import museum.util.SubjectLogoUtil
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Formatting

import java.util.stream.Collectors

import static org.bukkit.Material.*

class Prefix {
    String prefix
    String title
    int rare
    String bonus

    Prefix(String prefix, String title, int rare, String bonus) {
        this.prefix = prefix
        this.title = title
        this.rare = rare
        this.bonus = bonus
    }

    Prefix(String prefix, String title, int rare) {
        this.prefix = prefix
        this.title = title
        this.rare = rare
        this.bonus = ''
    }
}

registerCommand 'donate' handle {
    Guis.open player, 'donate', player
}

registerCommand 'prefixes' handle {
    Guis.open player, 'prefixes', player
}

void givePrefix(User owner) {

}

def prefixes = [
        new Prefix('䂋', 'Любовь', 3, '§l40% §fполучить +§b1 опыт'),
        new Prefix('㧥', 'Бывший бомж', 3, '§f+§620`000\$ §e ежедневной награды'),
        new Prefix('㕐', '§bПопаду на луну', 3, '§b+20% §fшанса получить камень'),
        new Prefix('㫐', 'Dead inside', 2),
        new Prefix('㕄', 'Радуга', 2),
        new Prefix('㗤', '§eЦирк', 2),
        new Prefix('㩑', '§lHIT!', 2),
        new Prefix('䀝', '§aЦель 40рб', 2),
        new Prefix('㯨', 'Не курю!', 1),
        new Prefix('㥗', 'Я пришелец', 1),
        new Prefix('㧵', '§aSTONKS', 1),
        new Prefix('㧋', '§cБомба', 1),
        new Prefix('㫩', 'Ок', 1),
        new Prefix('䀰', 'Спортивный', 1),
        new Prefix('㕾', '§cМухомор', 1),
        new Prefix('䀀', '§atoxic', 1),
        new Prefix('㗨', 'Консольщик', 1),
        new Prefix('㗧', 'Лицемер', 1),
        new Prefix('㛳', 'АУ', 1),
        new Prefix('㗯', 'Люблю музыку', 1),
]

Guis.register 'prefixes', {
    if (!(context instanceof Player))
        return
    def user = App.app.getUser(context)

    title 'Выбор префиксов'
    layout """
        XXXXXXXXX
        XXX-RRRRR
        -EEE-O-L-
        """
    button 'L' icon {
        item CLAY_BALL
        text '§cНазад'
        nbt.other = "cancel"
    } leftClick {
        performCommand("gui main")
    }
    button 'O' icon {
        item END_CRYSTAL
        text """
        §bСлучайный префикс §fза §e10'000'000\$
        
        §7Получите случайный префикс!
        
        Если такой префикс уже был?
        - §eВы получите §6§l50`000\$

        Каждое §dпятое §fоткрытие §dгарантирует
        §6редкий §fили §dэпичный §fпрефикс
        """
    } leftClick {
        if (user.money >= 10000000) {
            user.giveMoney(-10000000)
            def randomPrefix = ListUtils.random(
                    user.prefixChestOpened % 5 == 0 ?
                            prefixes.stream()
                                    .filter(prefix -> prefix.rare > 1)
                                    .collect(Collectors.toList()) :
                            prefixes
            )
            boolean flag = true
            // Если такой префикс уже есть
            for (def prefix in user.info.prefixes) {
                if (prefix.contains(randomPrefix.prefix)) {
                    flag = false
                    user.giveMoney(50000)
                    Anime.topMessage user.handle(), "Получен дубликат ${randomPrefix.prefix}, §aваша награда §6§l50`000\$"
                    break
                }
            }
            // Если такого префикса еще не было
            if (flag) {
                user.info.prefixes.add(randomPrefix.prefix)
                user.prefix = randomPrefix.prefix
                Anime.topMessage user.handle(), "Получен новый ${randomPrefix.prefix} " + (randomPrefix.rare > 1 ? randomPrefix.rare == 2 ? '§6редкий' : '§dэпический' : '') + " §fпрефикс! ${randomPrefix.title}"
            }
            user.prefixChestOpened = user.prefixChestOpened + 1
            closeInventory()
        } else
            AnimationUtil.buyFailure(user)
    }
    def counter = 0
    3.times {
        def prefix = prefixes.get(counter)
        def have = user.info.prefixes.contains(prefix.prefix)
        button 'E' icon {
            item EMERALD
            text """[ ${prefix.prefix} §f] ${prefix.title} ${have ? '§aВЫБРАТЬ' : ''}
        §7Выпадает из 'Случайный префикс'

        §7Редкость: §dэпический
        §7Бонус: ${prefix.bonus}
        """
        } leftClick {
            performCommand('changeprefix ' + prefix.prefix)
        }
        counter = counter + 1
    }
    5.times {
        def prefix = prefixes.get(counter)
        def have = user.info.prefixes.contains(prefix.prefix)
        button 'R' icon {
            item GOLD_INGOT
            text """[ ${prefix.prefix} §f] ${prefix.title} ${have ? '§aВЫБРАТЬ' : ''}
        §7Выпадает из 'Случайный префикс'

        §7Редкость: §bредкий
        §7Бонус: §cотсутствует
        """
        } leftClick {
            performCommand('changeprefix ' + prefix.prefix)
        }
        counter = counter + 1
    }
    12.times {
        def prefix = prefixes.get(counter)
        def have = user.info.prefixes.contains(prefix.prefix)
        button 'X' icon {
            item IRON_INGOT
            text "[ ${prefix.prefix} §f] ${prefix.title} ${have ? '§aВЫБРАТЬ' : ''}"
            text """    
                                
            §7Редкость: §aобычный
            §7Бонус: §cотсутствует
            """
        } leftClick {
            if (user.prefix && user.prefix.contains('LS'))
                return
            boolean flag = true
            for (def line in user.info.prefixes) {
                if (line.contains(prefix.prefix)) {
                    user.setPrefix(prefix.prefix)
                    closeInventory()
                    flag = false
                    break
                }
            }
            if (flag && user.crystal >= 10000) {
                user.crystal = user.crystal - 10000
                user.prefixes.add(prefix.prefix)
                user.setPrefix(prefix.prefix)
                closeInventory()
            }
        }
        counter = counter + 1
    }
}

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
            def randomPrefix = ListUtils.random(
                    user.prefixChestOpened % 5 == 0 ?
                            prefixes.stream()
                                    .filter(prefix -> prefix.rare > 1)
                                    .collect(Collectors.toList()) :
                            prefixes
            )
            boolean flag = true
            // Если такой префикс уже есть
            for (def prefix in user.info.prefixes) {
                if (prefix.contains(randomPrefix.prefix)) {
                    flag = false
                    user.giveMoney(50000)
                    Anime.topMessage user.handle(), "Получен дубликат ${randomPrefix.prefix}, §aваша награда §6§l50`000\$"
                    break
                }
            }
            // Если такого префикса еще не было
            if (flag) {
                user.info.prefixes.add(randomPrefix.prefix)
                user.prefix = randomPrefix.prefix
                Anime.topMessage user.handle(), "Получен новый ${randomPrefix.prefix} " + (randomPrefix.rare > 1 ? randomPrefix.rare == 2 ? '§6редкий' : '§dэпический' : '') + " §fпрефикс! ${randomPrefix.title}"
            }
            user.prefixChestOpened = user.prefixChestOpened + 1
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
