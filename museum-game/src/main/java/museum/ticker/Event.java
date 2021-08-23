package museum.ticker;

/**
 * @author Рейдж 23.08.2021
 * @project museum
 */
public interface Event extends Ticked {

    void start();

    void end();
}
