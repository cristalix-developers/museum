@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.gui

import implario.ListUtils
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.protocol.GlowColor
import me.func.mod.conversation.ModTransfer
import museum.data.PickaxeType
import museum.fragment.Gem
import museum.fragment.GemType
import museum.fragment.Meteorite
import museum.museum.subject.skeleton.Fragment
import museum.player.User
import museum.player.prepare.BeforePacketHandler
import museum.player.prepare.PreparePlayerBrain
import museum.prototype.Managers
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack

static void giveMeteorite(User owner) {
    def meteor = new Meteorite("meteor_" + ListUtils.random(Meteorite.Meteorites.values()).name())
    meteor.give(owner)
    new ModTransfer()
            .integer(1)
            .item(CraftItemStack.asNMSCopy(meteor.item))
            .string(ChatColor.stripColor(meteor.item.getItemMeta().displayName))
            .string(getRare(meteor.item.getItemMeta().displayName))
            .send("lootbox", owner.handle())
}

static void giveBone(User owner) {
    def proto = ListUtils.random(new ArrayList<>(Managers.skeleton))
    Fragment fragment = ListUtils.random(proto.getFragments().toArray(new Fragment[0]))
    def skeleton = owner.getSkeletons().supply(proto)
    skeleton.getUnlockedFragments().add(fragment)
    new ModTransfer()
            .integer(1)
            .item(CraftItemStack.asNMSCopy(proto.item))
            .string(ChatColor.stripColor(proto.title))
            .string(getRare(proto.title))
            .send("lootbox", owner.handle())
}

static void giveGem(User owner) {
    def gem = new Gem(ListUtils.random(GemType.values()).name() + ":" + (0.6 + (1.0 - 0.6) * Math.random()) + ":10000")
    gem.give(owner)
    new ModTransfer()
            .integer(1)
            .item(CraftItemStack.asNMSCopy(gem.item))
            .string(ChatColor.stripColor(gem.type.title + " " + Math.round(gem.rarity * 100F) + "%"))
            .string(getRare(gem.type.title))
            .send("lootbox", owner.handle())
}

static void giveMultiBox(User owner) {
    if (Math.random() < 0.1) {
        owner.pickaxeType = PickaxeType.LEGENDARY
        museum.player.pickaxe.PickaxeType.valueOf(owner.pickaxeType.name)
        new ModTransfer()
                .integer(1)
                .item(PreparePlayerBrain.getPickaxeImage(PickaxeType.LEGENDARY))
                .string(ChatColor.stripColor("⭐⭐⭐ §b§lЛегендарная кирка"))
                .string(getRare("⭐⭐⭐ §b§lЛегендарная кирка"))
                .send("lootbox", owner.handle())
    } else {
        Anime.itemTitle(owner.handle(), BeforePacketHandler.EMERGENCY_STOP, "Не удача", "Эх..Вам не повезло :(", 3.0);
        Glow.animate(owner.getPlayer(), 3.5, GlowColor.RED)
    }
}



static String getRare(String string) {
    return string.contains("⭐⭐⭐") ? "LEGENDARY" : string.contains("⭐⭐") ? "EPIC" : "RARE"
}
