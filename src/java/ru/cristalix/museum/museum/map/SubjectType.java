package ru.cristalix.museum.museum.map;

import clepto.cristalix.MapServiceException;
import lombok.RequiredArgsConstructor;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.museum.subject.CollectorSubject;
import ru.cristalix.museum.museum.subject.SimpleSubject;
import ru.cristalix.museum.museum.subject.SkeletonSubject;
import ru.cristalix.museum.museum.subject.Subject;

@RequiredArgsConstructor
public enum SubjectType {

    SKELETON_CASE(SkeletonSubject::new),
    DECORATION(SimpleSubject::new),
    COLLECTOR(CollectorSubject::new),
    MARKER(MarkerSubject::new);

    private final Provider provider;

    public Subject provide(Museum museum, SubjectInfo info) {
        return provider.provide(museum, info);
    }

    public static SubjectType byString(String string) {
        string = string.replace('-', '_').toUpperCase();
        for (SubjectType value : values()) {
            if (value.name().startsWith(string)) return value;
        }
        throw new MapServiceException("Subject type '" + string + "' is not a valid type.");
    }

    public interface Provider {

        Subject provide(Museum museum, SubjectInfo info);

    }
}
