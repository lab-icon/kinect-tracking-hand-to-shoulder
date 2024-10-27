package com.icon.chick.utils.kinect;

import com.icon.chick.App;
import KinectPV2.KJoint;
import KinectPV2.KinectPV2;
import processing.core.*;
import java.util.*;

public class CoordinateMapper {
    private final App app;
    private final KinectPV2 kinect;

    private static final float SMOOTHING_FACTOR = 0.2f;

    public CoordinateMapper(App app, KinectPV2 kinect) {
        this.app = app;
        this.kinect = kinect;
    }

    public PVector mapCoordinates(KJoint joint) {
        float x = joint.getX(), y = joint.getY(), z = joint.getZ();
        if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z) ||
                Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
            return new PVector(Float.NaN, Float.NaN, Float.NaN);
        }

        float mappedX = PApplet.map(x, 0, kinect.getColorImage().width, 0, this.app.width);
        float mappedY = PApplet.map(y, 0, kinect.getColorImage().height, 0, this.app.height);
        float mappedZ = PApplet.map(z, 0, 4500, 0, 100); // 4500 is the max depth value

        return new PVector(mappedX, mappedY, mappedZ);
    }

    public MappedCoordinates mapToBox(PVector hand, PVector shoulder, float shoulderDistance) {
        PVector handRelative = hand.copy().sub(shoulder);
        float mappedX = PApplet.map(handRelative.x, shoulderDistance, -shoulderDistance, -1, 1);
        float mappedY = PApplet.map(handRelative.y, shoulderDistance, -shoulderDistance, -1, 1);
        PVector original = new PVector(mappedX, mappedY);

        float correctedX = Math.max(-1, Math.min(1, mappedX));
        float correctedY = Math.max(-1, Math.min(1, mappedY));
        PVector corrected = new PVector(correctedX, correctedY);

        return new MappedCoordinates(original, corrected);
    }

    public PVector smoothHandPositions(List<PVector> handHistory) {
        if (handHistory.isEmpty()) {
            return new PVector(Float.NaN, Float.NaN, Float.NaN);
        }

        PVector smoothedPosition = handHistory.getFirst().copy();
        for (int i = 1; i < handHistory.size(); i++) {
            PVector currentPosition = handHistory.get(i);
            smoothedPosition.x = SMOOTHING_FACTOR * currentPosition.x + (1 - SMOOTHING_FACTOR) * smoothedPosition.x;
            smoothedPosition.y = SMOOTHING_FACTOR * currentPosition.y + (1 - SMOOTHING_FACTOR) * smoothedPosition.y;
            smoothedPosition.z = SMOOTHING_FACTOR * currentPosition.z + (1 - SMOOTHING_FACTOR) * smoothedPosition.z;
        }

        return smoothedPosition;
    }
}
