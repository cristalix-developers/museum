@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui


import implario.ListUtils
import me.func.mod.conversation.ModTransfer
import museum.fragment.Gem
import museum.fragment.GemType
import museum.fragment.Meteorite
import museum.player.User
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack

static void giveDrop(User owner) {
    def gem = new Gem(ListUtils.random(GemType.values()).name() + ":" + (Math.random() * 1.1) + ":10000")
    gem.give(owner)
    def meteor = new Meteorite("meteor_" + ListUtils.random(Meteorite.Meteorites.values()).name())
    meteor.give(owner)

    new ModTransfer()
            .integer(2)
            .item(CraftItemStack.asNMSCopy(gem.item))
            .string(ChatColor.stripColor(gem.type.title + " " + Math.round(gem.rarity * 100F) + "%"))
            .string(getRare(gem.type.title))
            .item(CraftItemStack.asNMSCopy(meteor.item))
            .string(ChatColor.stripColor(meteor.item.getItemMeta().displayName))
            .string(getRare(meteor.item.getItemMeta().displayName))
            .send("lootbox", owner.handle())
}

static String getRare(String string) {
    return string.contains("⭐⭐⭐") ? "LEGENDARY" : string.contains("⭐⭐") ? "EPIC" : "RARE"
}
