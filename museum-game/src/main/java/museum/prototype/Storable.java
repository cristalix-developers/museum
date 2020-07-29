package museum.prototype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import museum.data.Info;
import museum.player.User;

import static lombok.AccessLevel.PROTECTED;

@RequiredArgsConstructor(access = PROTECTED)
public abstract class Storable<I extends Info, P extends Prototype> {

	@Getter
	protected final P prototype;

	protected final I cachedInfo;

	@Getter
	protected final User owner;

	protected abstract void updateInfo();

}
