@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import museum.App
import museum.player.prepare.PreparePlayerBrain
import org.bukkit.event.player.PlayerFishEvent

on PlayerFishEvent, {
    if (!getHook().onGround)
        return
    if (getHook().location.distanceSquared(player.location) > 225)
        return
    player.setVelocity(getHook().location.subtract(player.location).toVector() * 0.3)
}

registerCommand 'crystal' handle {
    def user = App.app.getUser(player)
    if (user.experience <= PreparePlayerBrain.EXPERIENCE)
        return
    user.setState(App.app.crystalExcavation)
}