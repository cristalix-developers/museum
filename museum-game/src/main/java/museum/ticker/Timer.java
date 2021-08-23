package museum.ticker;

import lombok.Getter;
import museum.App;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @author Рейдж 23.08.2021
 * @project museum
 */
public class Timer {

    @Getter
    private final List<Event> events;

    public Timer(List<Event> events) {
        this.events = events;

        new BukkitRunnable() {
            @Override
            public void run() {
                Event.TIME.getAndIncrement();

                if (Event.TIME.get() == 99999999)
                    Event.TIME.set(0);

                events.forEach(event -> {
                    if (Event.TIME.get() % event.startTime() == 0)
                        event.start();
                    else if (Event.TIME.get() % event.endTime() == 0)
                        event.end();
                });
            }
        }.runTaskTimer(App.getApp(), 0, 20);
    }
}
