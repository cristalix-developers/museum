@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command.staff

import museum.App
import museum.discord.Bot
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.metadata.FixedMetadataValue
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.realm.IRealmService

def app = App.app

def vanishList = new ArrayList()

registerCommand 'vanish' handle {
    def user = app.getUser(player.uniqueId)
    def real = player as Player
    def realm = IRealmService.get().getCurrentRealmInfo().getRealmId()
    def isStaffPlayer = IPermissionService.get().isStaffMember(player.uniqueId)

    if ((user.prefix && user.prefix.contains('㗒')) || player.op || isStaffPlayer) {
        if (vanishList.contains(real)) {
            vanishList.remove(real)

            Bot.sendNormalMessage(realm.getRealmName(), "`" + player.getDisplayName() + " зашёл в игру`")

            real.sendMessage(Formatting.fine("§bВас снова §cвидно§b!"))
            real.removeMetadata("vanish", app)

            PacketPlayOutPlayerInfo add = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.handle)

            Bukkit.getOnlinePlayers().forEach {
                it.showPlayer(app, real)
                (it as CraftPlayer).handle.playerConnection.sendPacket(add)
            }
        } else {
            vanishList.add(real)

            Bot.sendNormalMessage(realm.getRealmName(), "`" + player.getDisplayName() + " вышел из игры`")

            real.sendMessage(Formatting.fine("§bВы были §cскрыты§b!"))
            real.setMetadata("vanish", new FixedMetadataValue(app, 1))

            PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, player.handle)

            Bukkit.getOnlinePlayers().forEach {
                it.hidePlayer(app, real)
                (it as CraftPlayer).handle.playerConnection.sendPacket(remove)
            }
        }
    }
}

on PlayerQuitEvent, {
    player.removeMetadata("vanish", app)
    vanishList.remove(player)
}