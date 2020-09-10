package museum.prototype;

import lombok.Getter;
import museum.App;

import java.util.*;

@Getter
public class PrototypeManager<T extends Prototype> {

	private final String name;
	private final Map<String, T> map = new HashMap<>();
	private final List<T> indexedList = new ArrayList<>();

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
		indexedList.add(prototype);
	}

	public T getByIndex(int index) {
		return indexedList.get(index);
	}

}
