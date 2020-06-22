package ru.cristalix.museum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.cristalix.museum.excavation.ExcavationManager;
import ru.cristalix.museum.museum.map.MuseumManager;
import ru.cristalix.museum.museum.subject.SubjectManager;
import ru.cristalix.museum.museum.subject.skeleton.SkeletonManager;

@Getter
@AllArgsConstructor
public enum Managers {

    SUBJECT(new SubjectManager()),
    MUSEUM(new MuseumManager()),
    SKELETON(new SkeletonManager()),
    EXCAVATION(new ExcavationManager()),
    ;

    private final Manager<? extends Prototype> manager;

}
