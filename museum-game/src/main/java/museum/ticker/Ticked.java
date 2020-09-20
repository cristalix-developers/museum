package museum.ticker;

import java.util.Random;

public interface Ticked {

	Random RANDOM = new Random();

	void tick(int... args);

}
