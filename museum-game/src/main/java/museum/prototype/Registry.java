package museum.prototype;

import lombok.RequiredArgsConstructor;
import museum.data.Info;
import museum.player.User;
import org.bukkit.Bukkit;
import ru.cristalix.core.account.IAccountService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Registry<I extends Info, P extends Prototype, S extends Storable<I, P>> implements Collection<S> {

	private final User owner;
	private final PrototypeManager<P> manager;
	private final Set<S> elements = new HashSet<>();
	private final Function<String, I> generator;
	private final Reader<I, P, S> reader;

	public void importInfos(Collection<I> data) {
		for (I info : data) importInfo(info);
	}

	public void importInfo(I info) {
		P prototype = manager.getPrototype(info.getPrototypeAddress());
		if (prototype == null) {
			Bukkit.getLogger().warning(owner.getUuid() + " (" + IAccountService.get().getNameByUuid(owner.getUuid()) +
					") had an illegal " + manager.getName() + " element, removing it.");
			return;
		}

		S storable = reader.read(prototype, info, owner);
		elements.add(storable);
	}

	public List<I> getData() {
		List<I> list = new ArrayList<>();
		for (S storable : elements) {
			storable.updateInfo();
			list.add(storable.cachedInfo);
		}
		return list;
	}

	public S get(P prototype) {
		for (S storable : elements) {
			if (storable.getPrototype() == prototype) return storable;
		}
		return null;
	}

	public S supply(P prototype) {
		S existing = this.get(prototype);
		if (existing != null) return existing;
		if (generator == null) return null;
		S generated = reader.read(prototype, generator.apply(prototype.getAddress()), owner);
		this.add(generated);
		return generated;
	}

	@Override
	public Iterator<S> iterator() {
		return elements.iterator();
	}

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return elements.contains(o);
	}

	public boolean containsEntry(P prototype) {
		return this.get(prototype) != null;
	}

	@Override
	public Object[] toArray() {
		return elements.toArray();
	}

	@Override
	public <T> T[] toArray(T[] ts) {
		return elements.toArray(ts);
	}

	@Override
	public boolean add(S element) {
		return elements.add(element);
	}

	@Override
	public boolean remove(Object o) {
		return elements.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> elements) {
		return this.elements.containsAll(elements);
	}

	@Override
	public boolean addAll(Collection<? extends S> collection) {
		return elements.addAll(collection);
	}

	@Override
	public boolean removeAll(Collection<?> elements) {
		return this.elements.removeAll(elements);
	}

	@Override
	public boolean retainAll(Collection<?> elements) {
		return this.elements.retainAll(elements);
	}

	@Override
	public void clear() {
		this.elements.clear();
	}

	@Override
	public Stream<S> stream() {
		return this.elements.stream();
	}

	@FunctionalInterface
	public interface Reader<I, P, T> {

		T read(P prototype, I info, User owner);

	}
}
