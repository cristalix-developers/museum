package museum.prototype;

import lombok.Getter;
import museum.App;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
public class PrototypeManager<T extends Prototype> {

	private final String name;
	private final Map<String, T> map = new HashMap<>();

	public PrototypeManager(String mapSignKey, BoxReader<T> reader) {
		this.name = mapSignKey;
		App.getApp().getMap().getBoxes(mapSignKey).entrySet().stream()
				.map(entry -> reader.readBox(entry.getKey(), entry.getValue()))
				.filter(Objects::nonNull)
				.forEach(this::registerPrototype);
	}

	public T getPrototype(String address) {
		return map.get(address);
	}

	private void registerPrototype(T prototype) {
		map.put(prototype.getAddress(), prototype);
	}
}
