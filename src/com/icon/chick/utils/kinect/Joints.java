/**
 * Project: ICON Lab - Kinect Hand to Shoulder Tracking
 * Author: Eduardo Monteiro @ ICON LAB
 * License: MIT License
 */

package com.icon.chick.utils.kinect;

import com.icon.chick.App;
import KinectPV2.*;
import org.jetbrains.annotations.NotNull;
import processing.core.*;

/**
 * The Joints class provides methods to calculate distances between joints,
 * and to draw joints, lines, and bounding boxes on the screen.
 */
public class Joints extends PApplet {

    App app;

    /**
     * Constructor for the Joints class.
     *
     * @param app The main application instance.
     */
    public Joints(App app){
        this.app = app;
    }

    /**
     * Calculates the distance between two joints.
     *
     * @param joint1 The first joint.
     * @param joint2 The second joint.
     * @return The distance between the two joints.
     */
    public float calcJointDistance(@NotNull KJoint joint1, @NotNull KJoint joint2) {
        return dist(joint1.getX(), joint1.getY(), joint2.getX(), joint2.getY());
    }

    /**
     * Draws a bounding box around a given vector with a specified distance.
     *
     * @param vector The center point of the bounding box.
     * @param distance The half-width of the bounding box.
     */
    public void drawBox(PVector vector, float distance) {
        this.app.pushMatrix();
        this.app.noFill();
        this.app.stroke((float) Math.random(),0,0);
        this.app.strokeWeight(2);
        this.app.rectMode(CENTER);
        this.app.rect(vector.x, vector.y, distance*2, distance*2);
        this.app.popMatrix();
    }

    /**
     * Draws a joint at the specified vector location.
     *
     * @param joint The location of the joint.
     */
    public void drawJoint(@NotNull PVector joint) {
        this.app.pushMatrix();
        this.app.translate(joint.x, joint.y, joint.z);
        this.app.ellipse(0, 0, 25, 25);
        this.app.popMatrix();
    }

    /**
     * Draws a line between two joints.
     *
     * @param joint1 The starting joint.
     * @param joint2 The ending joint.
     */
    public void drawLine(@NotNull PVector joint1, @NotNull PVector joint2) {
        this.app.pushMatrix();
        this.app.fill(0, 0, 255);
        this.app.line(joint1.x, joint1.y, joint1.z, joint2.x, joint2.y, joint2.z);
        this.app.popMatrix();
    }

    /**
     * Draws a bounding box in the body space defined by the given joints and height.
     *
     * @param joint The center joint of the bounding box.
     * @param shoulderJoint1 The first shoulder joint.
     * @param shoulderJoint2 The second shoulder joint.
     * @param boxHeight The height of the bounding box.
     */
    public void drawBodySpace(@NotNull PVector joint, PVector shoulderJoint1, PVector shoulderJoint2, float boxHeight) {
        this.app.pushMatrix();
        this.app.noFill();
        this.app.strokeWeight(10);
        this.app.stroke(0, 0, 225);
        this.app.translate(joint.x, joint.y);
        this.app.rectMode(CENTER);
        this.app.rect(0, 0, PVector.dist(shoulderJoint1, shoulderJoint2) * 4, boxHeight * 2);
        this.app.popMatrix();
    }
}