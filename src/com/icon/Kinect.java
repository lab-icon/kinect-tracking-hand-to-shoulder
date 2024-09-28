package com.icon;

import processing.core.*;
import KinectPV2.*;
import com.icon.utils.kinect.Joints;

import java.util.ArrayList;

public class Kinect extends PApplet {
    private final App app;
    KinectPV2 kinect;
    Joints joints;
    Boolean isCalibrated;

    float shoulderToHandDistance;

    public Kinect(App app) {
        this.app = app;
        this.joints = new Joints(app);
        this.isCalibrated = false;

        kinect = new KinectPV2(this.app);

        kinect.enableSkeletonColorMap(true);
        kinect.enableColorImg(true);

        kinect.init();
    }

    public float calibrate(KJoint[] kJoints) {
        // get vector shoulder-hand
        try {
            PGraphics pg = this.app.createGraphics(this.app.width, this.app.height);
            System.out.println("CALIBRATING");

            pg.beginDraw();
            pg.text("CALIBRATING...", (float) this.app.width /2, (float) this.app.height /2);
            pg.endDraw();

            this.app.image(pg, 0, 0);
            Thread.sleep(3000);
            pg.clear();


            float shoulderToHandDistance = this.joints.calcJointDistance(kJoints[KinectPV2.JointType_ShoulderRight], kJoints[KinectPV2.JointType_HandRight]);

            pg.beginDraw();
            pg.text("CALIBRATED: " + shoulderToHandDistance, (float) this.app.width /2, (float) this.app.height /2);
            System.out.println("CALIBRATED: " + shoulderToHandDistance);
            pg.endDraw();

            this.app.image(pg, 0, 0);
            Thread.sleep(3000);
            pg.clear();

            this.isCalibrated = true;
            return shoulderToHandDistance;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void draw() {
        this.app.image(kinect.getColorImage(), 0, 0, this.app.width, this.app.height);

        ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();
        for (KSkeleton skeleton : skeletonArray) {
            if (skeleton.isTracked()) {
                KJoint[] kJoints = skeleton.getJoints();

                float shoulderToHandDistance;

                if (!isCalibrated) {
                    shoulderToHandDistance = calibrate(kJoints);
                }

                int col = skeleton.getIndexColor();
                this.app.fill(col);
                this.app.stroke(col);
                this.joints.drawBodyPoint(kJoints);

                this.joints.drawLine(kJoints[KinectPV2.JointType_SpineShoulder], kJoints[KinectPV2.JointType_HandLeft]);
                this.joints.drawLine(kJoints[KinectPV2.JointType_SpineShoulder], kJoints[KinectPV2.JointType_HandRight]);

                this.joints.drawLine(kJoints[KinectPV2.JointType_ShoulderLeft], kJoints[KinectPV2.JointType_ShoulderRight]);

                this.joints.drawHandPoint(kJoints[KinectPV2.JointType_HandLeft]);
                this.joints.drawHandPoint(kJoints[KinectPV2.JointType_HandRight]);

                this.joints.drawBodySpace(kJoints[KinectPV2.JointType_SpineShoulder], kJoints[KinectPV2.JointType_ShoulderRight], kJoints[KinectPV2.JointType_ShoulderLeft]);
            }
        }
    }

    public void showFPS() {
        this.app.text("FPS: " + frameRate, 50, 50);
    }

}
