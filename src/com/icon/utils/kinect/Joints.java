package com.icon.utils.kinect;
import KinectPV2.KinectPV2;
import com.icon.App;

import KinectPV2.*;
import org.jetbrains.annotations.NotNull;
import processing.core.PApplet;

public class Joints extends PApplet {

    App app;

    public Joints(App app){
        this.app = app;
    }

    public float calcJointDistance(@NotNull KJoint joint1, @NotNull KJoint joint2) {
        return dist(joint1.getX(), joint1.getY(), joint2.getX(), joint2.getY());
    }

    public void drawBodyPoint(KJoint[] joints) {
        drawJoint(joints, KinectPV2.JointType_SpineShoulder);
    }

    public void drawJoint(KJoint @NotNull [] joints, int jointType) {
        this.app.pushMatrix();
        this.app.translate(joints[jointType].getX(), joints[jointType].getY(), joints[jointType].getZ());
        this.app.ellipse(0, 0, 25, 25);
        this.app.popMatrix();
    }

    public void drawLine(@NotNull KJoint joint1, @NotNull KJoint joint2) {
        this.app.pushMatrix();
        this.app.fill(0,0,255);
        this.app.line(joint1.getX(), joint1.getY(), joint1.getZ(), joint2.getX(), joint2.getY(), joint2.getZ());
        this.app.popMatrix();
    }

    public void drawBodySpace(@NotNull KJoint joint, @NotNull KJoint shoulderJoint1,@NotNull KJoint shoulderJoint2){
        this.app.pushMatrix();
        this.app.noFill();
        this.app.strokeWeight(10);
        this.app.stroke(0, 0, 225);
        this.app.translate(joint.getX(), joint.getY());
        this.app.rectMode(CENTER);
        this.app.rect(0, 0, calcJointDistance(shoulderJoint1, shoulderJoint2) * 4, 200);
        this.app.popMatrix();
    }

    public void drawHandPoint(@NotNull KJoint joint) {
        this.app.noStroke();
        handState(joint.getState());
        this.app.pushMatrix();
        this.app.translate(joint.getX(), joint.getY(), joint.getZ());
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
