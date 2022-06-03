package museum.discord.events;

import lombok.val;
import museum.data.UserInfo;
import museum.util.DiscordUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;


public class OnMessageReceivedEvent extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        TextChannel channel = event.getTextChannel();
        if (channel.getId().equals("981922048446201976") || channel.getId().equals("982265575583657984")) {
            Member member = event.getMember();
            if (member != null && !member.getId().equals("981261397734330368")) {
                UserInfo userInfo = DiscordUtil.getGameUser(member.getId());
                if (userInfo != null) {
                    val player = Bukkit.getPlayer(userInfo.getUuid());
                    val set = new HashSet<Player>();
                    set.add(player);

                    AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(true, player,
                            event.getMessage().getContentRaw(),
                            set);

                    Bukkit.getPluginManager().callEvent(chatEvent);
                }
            }

        }

    }

}
