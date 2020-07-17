package ru.cristalix.museum.prototype;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import ru.cristalix.core.account.IAccountService;
import ru.cristalix.museum.data.Info;
import ru.cristalix.museum.player.User;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Registry<I extends Info, P extends Prototype, S extends Storable<I, P>> implements Iterable<S> {

	private final User owner;
	private final PrototypeManager<P> manager;
	private final Set<S> elements = new HashSet<>();
	private final Function<String, I> generator;
	private final Reader<I, P, S> reader;

	public void addAll(Collection<I> data) {
		for (I info : data) add(info);
	}

	public void add(I info) {
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
		if (generator == null) return null;
		S generated = reader.read(prototype, generator.apply(prototype.getAddress()), owner);
		elements.add(generated);
		return generated;
	}

	@Override
	public Iterator<S> iterator() {
		return elements.iterator();
	}

	public int size() {
		return elements.size();
	}

	public Stream<S> stream() {
		return elements.stream();
	}

	@FunctionalInterface
	public interface Reader<I, P, T> {

		T read(P prototype, I info, User owner);

	}
}
