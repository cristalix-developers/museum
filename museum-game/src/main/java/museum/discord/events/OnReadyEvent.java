package museum.discord.events;

import lombok.val;
import museum.discord.Bot;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class OnReadyEvent extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        val bot = Bot.getSelfUser();
        val jda = Bot.getJda();
        // Реакция с бустом
        val reaction = jda.getEmoteById(986917472257048596L);

        // Автодобавление реакций на сообщения (на всякий случай)
        Bot.getJda().getTextChannelById(981918245487513690L).addReactionById(986913965391101962L, "\uD83D\uDCD1").complete();
        Bot.getJda().getTextChannelById(981918245487513690L).addReactionById(986913965391101962L, reaction).complete();

        System.out.println("Discord bot [" + bot.getName() + "] is ready!");
    }

}
