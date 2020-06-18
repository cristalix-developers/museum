package ru.func.museum.player;

import delfikpro.exhibit.Exhibit;
import delfikpro.exhibit.Fragment;
import ru.func.museum.App;
import ru.func.museum.data.SkeletonInfo;

import java.util.Arrays;
import java.util.List;

public class Skeleton {

	private final SkeletonInfo info;
	private final Exhibit exhibit;
	private final List<Fragment> fragments;

	public Skeleton(SkeletonInfo info) {
		this.info = info;
		this.exhibit = App.getApp().getExhibitManager().getExhibit(info.getAddress());
		this.fragments = exhibit.getFragments().stream().filter();
	}

}
