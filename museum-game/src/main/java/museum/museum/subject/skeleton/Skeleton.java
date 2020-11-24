package museum.museum.subject.skeleton;

import lombok.Getter;
import museum.data.model.SkeletonModel;
import museum.player.User;
import museum.prototype.Storable;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Skeleton extends Storable<SkeletonModel, SkeletonPrototype> {

	private final Set<Fragment> unlockedFragments;

	public Skeleton(SkeletonPrototype prototype, SkeletonModel info, User user) {
		super(prototype, info, user);
		this.unlockedFragments = prototype.getFragments().stream()
				.filter(e -> info.getUnlockedFragmentAddresses().contains(e.getAddress()))
				.collect(Collectors.toSet());
	}

	@Override
	public void updateInfo() {
		cachedInfo.setUnlockedFragmentAddresses(unlockedFragments.stream()
				.map(Fragment::getAddress)
				.collect(Collectors.toList())
		);
	}

}
