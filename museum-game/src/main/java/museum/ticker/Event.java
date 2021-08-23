package museum.ticker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Рейдж 23.08.2021
 * @project museum
 */
public interface Event {

    AtomicInteger TIME = new AtomicInteger(0);

    void start();

    void end();

    int startTime();

    int endTime();
}
