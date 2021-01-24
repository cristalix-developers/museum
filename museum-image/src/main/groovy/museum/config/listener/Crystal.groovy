@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.listener

import museum.App
import museum.player.prepare.PreparePlayerBrain
import museum.util.MessageUtil
import org.bukkit.Material
import org.bukkit.event.player.PlayerFishEvent

import static museum.boosters.BoosterType.EXP

on PlayerFishEvent, {
    if (state == PlayerFishEvent.State.CAUGHT_FISH) {
        expToDrop = 0
        def user = App.app.getUser(player)
        def exp = (int) (Math.ceil(Math.random() * 3 + 3) * App.app.playerDataManager.calcGlobalMultiplier(EXP))
        user.giveExperience(exp)
        MessageUtil.find('fishing').set('exp', exp).send(user)
        return
    }
    def hookLocation = getHook().location.clone()
    if (hookLocation.subtract(0, 0.3, 0).block.type == Material.AIR)
        return
    if (getHook().location.distanceSquared(player.location) > 300)
        return
    player.setVelocity(hookLocation.subtract(player.location).toVector() * 0.3)
}

registerCommand 'crystal' handle {
    def user = App.app.getUser(player)
    if (user.experience <= PreparePlayerBrain.EXPERIENCE * 3)
        return
    user.setState(App.app.crystalExcavation)
}