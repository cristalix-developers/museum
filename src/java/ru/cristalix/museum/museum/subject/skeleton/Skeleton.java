package ru.cristalix.museum.museum.subject.skeleton;

import lombok.Getter;
import ru.cristalix.museum.prototype.Storable;
import ru.cristalix.museum.data.SkeletonInfo;
import ru.cristalix.museum.player.User;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Skeleton extends Storable<SkeletonInfo, SkeletonPrototype> {

	private final Set<Fragment> unlockedFragments;

	public Skeleton(SkeletonPrototype prototype, SkeletonInfo info, User user) {
		super(prototype, info, user);
		this.unlockedFragments = prototype.getFragments().stream()
				.filter(e -> info.getUnlockedFragmentAddresses().contains(e.getAddress()))
				.collect(Collectors.toSet());
	}

	public void updateInfo() {
		cachedInfo.setUnlockedFragmentAddresses(unlockedFragments.stream()
				.map(Fragment::getAddress)
				.collect(Collectors.toList())
		);
	}

}
