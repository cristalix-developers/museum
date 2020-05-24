package ru.func.museum.museum.space.viewer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpaceViewerType {
    SIMPLE(new SimpleSpaceViewer()),
    SKELETON(new SkeletonSpaceViewer()),;

    private SpaceViewer spaceVisitor;
}
