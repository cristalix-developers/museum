package museum.player;

import clepto.ListUtils;
import museum.App;
import museum.util.ChunkWriter;
import ru.cristalix.core.scoreboard.SimpleBoardObjective;

import java.util.Collection;

public interface State {

	void setupScoreboard(User user, SimpleBoardObjective obj);

	void enterState(User user);

	void leaveState(User user);

	default void rewriteChunk(User user, ChunkWriter chunkWriter) {}

	default Collection<User> getUsers() {
		return ListUtils.filter(App.getApp().getUsers(), user -> user.getState() == this);
	}
}
