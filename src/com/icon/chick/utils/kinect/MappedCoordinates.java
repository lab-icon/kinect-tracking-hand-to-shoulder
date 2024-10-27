package com.icon.chick.utils.kinect;

import processing.core.PVector;

public class MappedCoordinates {
    public final PVector original;
    public final PVector corrected;

    public MappedCoordinates(PVector original, PVector corrected) {
        this.original = original;
        this.corrected = corrected;
    }
}
