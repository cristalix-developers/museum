package museum.service.conduct;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import museum.packages.MuseumPackage;
import museum.packages.UserRequestJoinPackage;
import museum.service.MuseumService;
import museum.service.user.ServiceUser;
import museum.utils.UtilNetty;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.TransferPlayerPackage;
import ru.cristalix.core.realm.RealmId;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class ConductService implements IConductService {

	private final MuseumService museumService;

	@SuppressWarnings ("rawtypes")
	private final Map<Class<? extends MuseumPackage>, PacketHandler> handlerMap = new HashMap<>();

	private final Map<RealmId, Realm> realms = new ConcurrentHashMap<>();

	@Override
	public void enable() {
		registerHandler(UserRequestJoinPackage.class, (source, packet) -> {
			Optional<Realm> bestRealm = this.getBestRealm();
			packet.setPassed(bestRealm.isPresent());
			bestRealm.ifPresent(realm -> {
				ISocketClient.get().write(new TransferPlayerPackage(packet.getUser(), realm.getId(), Collections.emptyMap()));
			});
			source.send(packet);
		});
	}

	@Override
	public Realm getRealm(RealmId realm) {
		return realms.get(realm);
	}

	@Override
	public void registerRealm(Realm realm) {
		realms.put(realm.getId(), realm);
	}

	@Override
	public void unregisterRealm(Realm realm) {
		realms.remove(realm.getId());
	}

	@Override
	public <T extends MuseumPackage> void registerHandler(Class<T> type, PacketHandler<T> handler) {
		handlerMap.put(type, handler);
	}

	@Override
	@SuppressWarnings ({"rawtypes", "unchecked"})
	public <T extends MuseumPackage> void handlePacket(Realm realm, T packet) {
		PacketHandler handler = handlerMap.get(packet.getClass());
		if (handler != null) handler.handle(realm, packet);
	}

	@Override
	public void broadcast(MuseumPackage packet) {
		TextWebSocketFrame frame = UtilNetty.toFrame(packet);
		for (Realm realm : getRealms()) realm.send(frame);
	}

	@Override
	public Collection<? extends Realm> getRealms() {
		return realms.values();
	}

	@Override
	public Optional<Realm> getBestRealm() {

		return museumService.getUserService().getUsers().stream() // Берём всех юзеров
				// Собираем в мапу: ключ - реалм, значение - 1, при повторении реалма значения суммируются
				.collect(toMap(ServiceUser::getRealm, user -> 1, Integer::sum))
				// Находим минимальное по значению (кол-ву юзеров) и берём ключ (реалм)
				.entrySet().stream().min(comparingByValue()).map(Entry::getKey);

	}

}
