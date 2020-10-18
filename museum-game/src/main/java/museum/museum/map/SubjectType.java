package museum.museum.map;

import clepto.bukkit.world.WorldConfigurationException;
import museum.data.SubjectInfo;
import museum.museum.subject.*;
import museum.player.User;

import java.util.ArrayList;
import java.util.List;

public class SubjectType<T extends Subject> {

	private static final List<SubjectType<?>> registry = new ArrayList<>();

	public static SubjectType<SkeletonSubject> SKELETON_CASE;
	public static SubjectType<Subject> DECORATION;
	public static SubjectType<CollectorSubject> COLLECTOR;
	public static SubjectType<MarkerSubject> MARKER;
	public static SubjectType<FountainSubject> FOUNTAIN;
	public static SubjectType<StallSubject> STALL;

	private final String address;
	private final Provider provider;

	public SubjectType(String address, Provider provider) {
		this.provider = provider;
		this.address = address;
		registry.add(this);
	}

	public static void init() {
		SKELETON_CASE = new SubjectType<>("skeleton-case", SkeletonSubject::new);
		DECORATION = new SubjectType<>("decoration", Subject::new);
		COLLECTOR = new SubjectType<>("collector", CollectorSubject::new);
		MARKER = new SubjectType<>("marker", MarkerSubject::new);
		FOUNTAIN = new SubjectType<>("fountain", FountainSubject::new);
		STALL = new SubjectType<>("stall", StallSubject::new);
	}

	public static SubjectType<?> byString(String query) {
		query = query.toLowerCase().replace("_", "-");
		for (SubjectType<?> subjectType : registry)
			if (subjectType.address.startsWith(query))
				return subjectType;
		throw new WorldConfigurationException("Subject type '" + query + "' is not a valid type.");
	}

	public Subject provide(SubjectPrototype prototype, SubjectInfo info, User user) {
		return provider.provide(prototype, info, user);
	}

	@FunctionalInterface
	public interface Provider {
		Subject provide(SubjectPrototype prototype, SubjectInfo info, User user);
	}
}
