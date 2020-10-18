@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import museum.App
import museum.museum.Museum
import museum.museum.map.SubjectType
import museum.worker.WorkerUtil
import net.minecraft.server.v1_12_R1.PacketPlayOutMount
import org.bukkit.inventory.EquipmentSlot

on PlayerUseUnknownEntityEvent, {
    if (hand == EquipmentSlot.OFF_HAND)
        return
    def user = App.app.getUser(player)
    WorkerUtil.acceptClick user, entityId
    def state = user.state
    if (state instanceof Museum) {
        state.getSubjects(SubjectType.COLLECTOR).forEach {
            if (it.piece.stand.id == entityId) {
                it.piece.stand.passengers.add(user.player.handle)
                state.users*.sendPacket(new PacketPlayOutMount(it.piece.stand))
                return
            }
        }
    }
}