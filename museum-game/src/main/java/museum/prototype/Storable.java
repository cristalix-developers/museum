package museum.prototype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import museum.data.Info;
import museum.player.User;

import static lombok.AccessLevel.PROTECTED;

@Getter
@RequiredArgsConstructor(access = PROTECTED)
public abstract class Storable<I extends Info, P extends Prototype> {

	protected final P prototype;

	protected final I cachedInfo;

	protected final User owner;

	protected void updateInfo() {}

}
