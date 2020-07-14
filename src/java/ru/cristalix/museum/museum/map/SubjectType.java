package ru.cristalix.museum.museum.map;

import clepto.cristalix.MapServiceException;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.subject.*;
import ru.cristalix.museum.player.User;

import java.util.ArrayList;
import java.util.List;

public class SubjectType<T extends Subject> {

	private static final List<SubjectType<?>> registry = new ArrayList<>();

	public static SubjectType<SkeletonSubject> SKELETON_CASE;
	public static SubjectType<Subject> DECORATION;
	public static SubjectType<CollectorSubject> COLLECTOR;
	public static SubjectType<MarkerSubject> MARKER;

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
	}

	public static SubjectType<?> byString(String query) {
		query = query.toLowerCase().replace("_", "-");
		for (SubjectType<?> subjectType : registry)
			if (subjectType.address.startsWith(query))
				return subjectType;
		throw new MapServiceException("Subject type '" + query + "' is not a valid type.");
	}

	public Subject provide(SubjectPrototype prototype, SubjectInfo info, User user) {
		return provider.provide(prototype, info, user);
	}

	public interface Provider {
		Subject provide(SubjectPrototype prototype, SubjectInfo info, User user);
	}
}
