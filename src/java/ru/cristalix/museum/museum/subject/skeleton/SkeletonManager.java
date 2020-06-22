package ru.cristalix.museum.museum.subject.skeleton;

import clepto.cristalix.Box;
import clepto.cristalix.WorldMeta;
import ru.cristalix.museum.App;
import ru.cristalix.museum.Manager;

public class SkeletonManager extends Manager<SkeletonPrototype> {

	public SkeletonManager() {
		super("model");
	}

	@Override
	protected SkeletonPrototype readBox(String address, Box box) {
		return new SkeletonPrototype(
				box.requireLabel("title").getTag(),
				box.requireLabel("size").getTagInt(),
				box.requireLabel("pieces").getTagInt(),
				Rarity.valueOf(box.requireLabel("size").getTag().toUpperCase()),
				address,
				box.requireLabel("origin")
		);
	}
}
