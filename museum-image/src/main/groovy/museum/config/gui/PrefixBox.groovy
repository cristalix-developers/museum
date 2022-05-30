@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import implario.ListUtils
import me.func.mod.Anime
import me.func.mod.conversation.ModTransfer
import museum.content.PrefixType
import museum.player.User
import org.bukkit.Material
import ru.cristalix.core.item.Items
import java.util.stream.Collectors


static void givePrefix(User user) {
    def prefixes = PrefixType.values()
    def rarePrefixes = prefixes.stream().filter(prefix -> prefix.rare > 1).collect Collectors.toList()
    def prefixFromLootbox = ListUtils.random(user.prefixChestOpened % 5 == 0 ? rarePrefixes : prefixes)

    // Проверка на уже имеющийся префикс
    boolean flag = true
    for (def alreadyExistingPrefix in user.info.prefixes) {
        if (alreadyExistingPrefix.contains(prefixFromLootbox.prefix)) {
            flag = false
            user.giveMoney(50000)
            Anime.topMessage user.handle(), "Получен дубликат ${prefixFromLootbox.prefix}, §aваша награда §6§l50,000\$"
            break
        }
    }

    // Если такого префикса еще не было
    if (flag) {
        user.info.prefixes.add(prefixFromLootbox.prefix)
        user.prefix = prefixFromLootbox.prefix
        def prefixRarity = prefixFromLootbox.rare
        def prefixMaterial = prefixRarity == 3 ? Material.EMERALD : prefixRarity == 2 ? Material.GOLD_INGOT : Material.IRON_INGOT
        def item = Items.builder().type(prefixMaterial).build()

        new ModTransfer()
                .integer(1)
                .item(item)
                .string(prefixFromLootbox.prefix + " " + prefixFromLootbox.title)
                .string(getRare(prefixRarity) + " префикс")
                .send("lootbox", user.handle())
    }

    user.prefixChestOpened = user.prefixChestOpened + 1
}

static String getRare(Integer rarity) {
    return rarity == 3 ? '§aЛегендарный' : rarity == 2 ? '§dЭпический' : '§6Редкий'
}
