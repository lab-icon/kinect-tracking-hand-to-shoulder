package com.icon.chick.utils.kinect;

import KinectPV2.KinectPV2;
import com.icon.chick.App;

import KinectPV2.*;
import org.jetbrains.annotations.NotNull;
import processing.core.*;

public class Joints extends PApplet {

    App app;

    public Joints(App app){
        this.app = app;
    }

    public float calcJointDistance(@NotNull KJoint joint1, @NotNull KJoint joint2) {
        return dist(joint1.getX(), joint1.getY(), joint2.getX(), joint2.getY());
    }

    public void drawBox(PVector vector, float distance) {
        this.app.pushMatrix();
        this.app.noFill();
        this.app.stroke((float) Math.random(),0,0);
        this.app.strokeWeight(2);
        this.app.rectMode(CENTER);
        this.app.rect(vector.x, vector.y, distance*2, distance*2);
        this.app.popMatrix();
    }

    public void drawJoint(@NotNull PVector joint) {
        this.app.pushMatrix();
        this.app.translate(joint.x, joint.y, joint.z);
        this.app.ellipse(0, 0, 25, 25);
        this.app.popMatrix();
    }

    public void drawLine(@NotNull PVector joint1, @NotNull PVector joint2) {
        this.app.pushMatrix();
        this.app.fill(0, 0, 255);
        this.app.line(joint1.x, joint1.y, joint1.z, joint2.x, joint2.y, joint2.z);
        this.app.popMatrix();
    }

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

    public void drawHandPoint(@NotNull PVector joint, int state) {
        this.app.noStroke();
        handState(state);
        this.app.pushMatrix();
        this.app.translate(joint.x, joint.y, joint.z);
        this.app.ellipse(0, 0, 70, 70);
        this.app.popMatrix();
    }

    void handState(int state) {
        switch (state) {
            case KinectPV2.HandState_Open:
                this.app.fill(0, 255, 0);
                break;
            case KinectPV2.HandState_Closed:
                this.app.fill(255, 0, 0);
                break;
            case KinectPV2.HandState_Lasso:
                this.app.fill(0, 0, 255);
                break;
            case KinectPV2.HandState_NotTracked:
                this.app.fill(100, 100, 100);
                break;
        }
    }
}
