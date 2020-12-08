@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener


import museum.App
import museum.player.prepare.PreparePlayerBrain
import museum.util.MessageUtil
import org.bukkit.event.player.PlayerFishEvent

on PlayerFishEvent, {
    if (state == PlayerFishEvent.State.CAUGHT_FISH) {
        expToDrop = 0
        def user = App.app.getUser(player)
        def exp = (int) Math.ceil(Math.random() * 3 + 2)
        user.giveExperience(exp)
        MessageUtil.find('fishing').set('exp', exp).send(user)
        return
    }
    if (!getHook().onGround)
        return
    if (getHook().location.distanceSquared(player.location) > 250)
        return
    player.setVelocity(getHook().location.subtract(player.location).toVector() * 0.3)
}

registerCommand 'crystal' handle {
    def user = App.app.getUser(player)
    if (user.experience <= PreparePlayerBrain.EXPERIENCE * 5)
        return
    user.setState(App.app.crystalExcavation)
}