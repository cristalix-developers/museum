package ru.cristalix.museum.skeleton;

import lombok.Data;
import lombok.experimental.Delegate;
import ru.cristalix.museum.App;
import ru.cristalix.museum.Storable;
import ru.cristalix.museum.data.SkeletonInfo;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class Skeleton implements Storable<SkeletonInfo> {

	@Delegate
	private final SkeletonInfo info;
	private final SkeletonPrototype skeletonPrototype;
	private final List<Fragment> unlockedFragments;

	@Override
	public SkeletonInfo generateInfo() {
		info.setUnlockedFragmentAddresses(unlockedFragments.stream()
				.map(Fragment::getAddress)
				.collect(Collectors.toList()));
		return info;
	}

	public Skeleton(SkeletonInfo info) {
		this.info = info;
		this.skeletonPrototype = App.getApp().getSkeletonManager().getExhibit(info.getAddress());
		this.unlockedFragments = skeletonPrototype.getFragments().stream()
				.filter(e -> info.getUnlockedFragmentAddresses().contains(e.getAddress()))
				.collect(Collectors.toList());
	}

}
