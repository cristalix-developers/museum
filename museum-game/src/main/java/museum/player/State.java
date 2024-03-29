package museum.player;

import implario.ListUtils;
import museum.App;
import museum.util.ChunkWriter;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;

import java.util.Collection;

public interface State {

	PotionEffect NIGHT_VISION = new PotionEffect(
			PotionEffectType.NIGHT_VISION,
			999999, 10, false, false
	);

	void enterState(User user);

	void leaveState(User user);

	boolean playerVisible();

	boolean nightVision();

	default void rewriteChunk(User user, ChunkWriter chunkWriter) {}

	default Collection<User> getUsers() {
		return ListUtils.filter(App.getApp().getUsers(), user -> user.getState() == this);
	}
}
