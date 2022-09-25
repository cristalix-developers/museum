@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import me.func.mod.Anime
import me.func.mod.conversation.ModTransfer
import me.func.protocol.world.marker.MarkerSign
import museum.util.SubjectLogoUtil
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemHeldEvent

import static museum.App.app

on PlayerItemHeldEvent, {
    def player = getPlayer() as Player

    def newSubject = SubjectLogoUtil.decodeItemStackToSubject(app.getUser(player), player.inventory.getItem(newSlot))
    def previousSubject = SubjectLogoUtil.decodeItemStackToSubject(app.getUser(player), player.inventory.getItem(previousSlot))

    if (previousSubject != null || newSubject == null) Anime.clearMarkers(player)

    if (newSubject != null) {
        var address = newSubject.getPrototype().getAddress()
        if (address.contains("big-case")) address = "big-case"
        if (address.contains("small-case")) address = "small-case"
        if (address.contains("relic")) address = "relic"
        if (address.contains("tree")) address = "tree"
        if (address.contains("collector")) address = "collector-free"
        def locations = app.getMap().getLabels("default", address)
        locations.addAll(app.getMap().getLabels("optional", address))

        locations.each {location ->
            def found = false
            app.getUser(player).getSubjects().each {subject ->
                if (found) return true
                if (subject.getAllocation() == null) return false

                subject.getAllocation().getAllocatedBlocks().each {block ->
                    if (block.x == location.x && block.y == location.y && block.z == location.z) {
                        found = true
                        return true
                    }
                }
            }
            def markerLocation = location.toCenterLocation()
            markerLocation.setY(location.y + 3)

            if (!found) {
                new ModTransfer()
                        .string(UUID.randomUUID().toString())
                        .putDouble(markerLocation.x)
                        .putDouble(markerLocation.y)
                        .putDouble(markerLocation.z)
                        .putDouble(40)
                        .string(MarkerSign.ARROW_DOWN.texture)
                        .send("func:marker-new", player)
            }
        }
    }
}