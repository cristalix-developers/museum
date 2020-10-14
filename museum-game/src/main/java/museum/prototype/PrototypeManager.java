package museum.prototype;

import lombok.Getter;
import museum.App;

import java.util.*;

@Getter
public class PrototypeManager<T extends Prototype> implements Collection<T> {

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

	@Override
	public Iterator<T> iterator() {
		return map.values().iterator();
	}

	@Override
	public int size() {
		return map.values().size();
	}

	@Override
	public boolean isEmpty() {
		return map.values().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return map.containsValue(o);
	}

	@Override
	public Object[] toArray() {
		return map.values().toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] t1s) {
		return map.values().toArray(t1s);
	}

	@Override
	public boolean add(T t) {
		registerPrototype(t);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return map.values().containsAll(collection);
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

}
