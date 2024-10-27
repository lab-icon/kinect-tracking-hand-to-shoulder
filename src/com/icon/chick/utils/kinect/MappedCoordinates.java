/**
 * Project: ICON Lab - Kinect Hand to Shoulder Tracking
 * Author: Eduardo Monteiro @ ICON LAB
 * License: MIT License
 */

package com.icon.chick.utils.kinect;

import processing.core.PVector;

/**
 * The MappedCoordinates class represents the original and corrected coordinates
 * of a point in 3D space.
 */
public class MappedCoordinates {
    /**
     * The original coordinates of the point.
     */
    public final PVector original;

    /**
     * The corrected coordinates of the point.
     */
    public final PVector corrected;

    /**
     * Constructor for the MappedCoordinates class.
     *
     * @param original The original coordinates of the point.
     * @param corrected The corrected coordinates of the point.
     */
    public MappedCoordinates(PVector original, PVector corrected) {
        this.original = original;
        this.corrected = corrected;
    }
}