package museum.discord.events;

import lombok.val;
import museum.discord.Bot;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class OnReadyEvent extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        val bot = Bot.getSelfUser();
        System.out.println("Discord bot [" + bot.getName() + "] is ready!");
    }

}
