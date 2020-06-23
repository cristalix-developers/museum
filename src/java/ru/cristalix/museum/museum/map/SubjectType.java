package ru.cristalix.museum.museum.map;

import clepto.cristalix.MapServiceException;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.subject.*;

import java.util.ArrayList;
import java.util.List;

public class SubjectType<T extends Subject> {

	private static final List<SubjectType<?>> registry = new ArrayList<>();

	public static SubjectType<SkeletonSubject> SKELETON_CASE;
	public static SubjectType<SimpleSubject> DECORATION;
	public static SubjectType<CollectorSubject> COLLECTOR;
	public static SubjectType<MarkerSubject> MARKER;

	public static void init() {
		SKELETON_CASE = new SubjectType<>("skeleton-case", SkeletonSubject::new);
		DECORATION = new SubjectType<>("decoration", SimpleSubject::new);
		COLLECTOR = new SubjectType<>("collector", CollectorSubject::new);
		MARKER = new SubjectType<>("marker", MarkerSubject::new);
	}

	private final String address;
	private final Provider provider;

	public SubjectType(String address, Provider provider) {
		this.provider = provider;
		this.address = address;
		registry.add(this);
	}

	public Subject provide(Museum museum, SubjectInfo info, SubjectPrototype prototype) {
		return provider.provide(museum, info, prototype);
	}

	public static SubjectType<?> byString(String query) {
		query = query.toLowerCase().replace("_", "-");
		for (SubjectType<?> subjectType : registry)
			if (subjectType.address.startsWith(query))
				return subjectType;
		throw new MapServiceException("Subject type '" + query + "' is not a valid type.");
	}

	public interface Provider {

		Subject provide(Museum museum, SubjectInfo info, SubjectPrototype prototype);

	}

}
