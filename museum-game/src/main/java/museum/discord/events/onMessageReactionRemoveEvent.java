package museum.discord.events;

import lombok.val;
import museum.util.DiscordUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class onMessageReactionRemoveEvent  extends ListenerAdapter {

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        val member = event.getMember();
        if (member == null)
            return;

        if (event.getMessageId().equals("986913965391101962") && !member.getId().equals("981261397734330368")) {
            val reactionName = event.getReaction().getReactionEmote().getName();
            val guild = event.getGuild();
            Role role = DiscordUtil.getRoleForReactionMessage(guild, reactionName);
            guild.removeRoleFromMember(member, role).queue();
        }
    }
}