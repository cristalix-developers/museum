@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import museum.App
import museum.museum.Museum
import museum.util.SubjectLogoUtil

registerCommand 'remove' handle {
    def user = App.app.getUser(player)
    def state = user.state

    if (state instanceof Museum) {
        def subject = SubjectLogoUtil.decodeItemStackToSubject(user, user.getInventory().getItemInHand())
        if (subject) {
            user.getInventory().setItemInHand(null)
            user.subjects.remove(subject)
            user.sendMessage("Удалила? Если видишь это напиши 789")
        }
    }
}

