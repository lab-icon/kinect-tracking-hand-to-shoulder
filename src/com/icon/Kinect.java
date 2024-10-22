package com.icon;

import processing.core.*;
import KinectPV2.*;
import com.icon.utils.kinect.Joints;

import java.util.*;

public class Kinect extends PApplet {
    private final App app;
    private final KinectPV2 kinect;
    private final Joints joints;
    private Boolean isCalibrated;

    private Map<Integer, Float> playerDistances;

    public Kinect(App app) {
        this.app = app;
        this.joints = new Joints(app);
        this.isCalibrated = false;
        this.playerDistances = new HashMap<>();

        kinect = new KinectPV2(this.app);

        kinect.enableSkeletonColorMap(true);
        kinect.enableColorImg(true);

        kinect.init();
    }

    private PVector mapCoordinates(float x, float y, float z) {
        if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z) ||
                Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(z)) {
            return new PVector(Float.NaN, Float.NaN, Float.NaN);
        }

        float mappedX = map(x, 0, kinect.getColorImage().width, 0, this.app.width);
        float mappedY = map(y, 0, kinect.getColorImage().height, 0, this.app.height);
        float mappedZ = map(z, 0, 4500, 0, 100); // 4500 is the max depth value

        return new PVector(mappedX, mappedY, mappedZ);
    }

    public void calibrate() {
        try {
            PGraphics pg = this.app.createGraphics(this.app.width, this.app.height);
            System.out.println("CALIBRATING");

            pg.beginDraw();
            pg.text("CALIBRATING...", (float) this.app.width / 2, (float) this.app.height / 2);
            pg.endDraw();

            this.app.image(pg, 0, 0);
            Thread.sleep(3000);
            pg.clear();

            ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();
            for (KSkeleton skeleton : skeletonArray) {
                if (skeleton.isTracked()) {
                    int playerId = skeleton.getIndexColor();
                    KJoint[] kJoints = skeleton.getJoints();

                    int numMeasurements = 10;
                    float totalDistance = 0;

                    for (int i = 0; i < numMeasurements; i++) {
                        float shoulderToHandDistance = this.joints.calcJointDistance(kJoints[KinectPV2.JointType_ShoulderRight], kJoints[KinectPV2.JointType_HandRight]);
                        totalDistance += shoulderToHandDistance;
                        Thread.sleep(100); // Short delay between measurements
                    }

                    float averageDistance = totalDistance / numMeasurements;
                    playerDistances.put(playerId, averageDistance);

                    pg.beginDraw();
                    pg.text("CALIBRATED: " + averageDistance, (float) this.app.width / 2, (float) this.app.height / 2);
                    System.out.println("CALIBRATED: " + averageDistance);
                    pg.endDraw();

                    this.app.image(pg, 0, 0);
                    pg.clear();
                }
            }

            this.isCalibrated = true;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Kinect.java
    private void removeUntrackedPlayers(ArrayList<KSkeleton> trackedSkeletons) {
        Set<Integer> trackedPlayerIds = new HashSet<>();
        for (KSkeleton skeleton : trackedSkeletons) {
            if (skeleton.isTracked()) {
                trackedPlayerIds.add(skeleton.getIndexColor());
            }
        }

        playerDistances.keySet().removeIf(playerId -> !trackedPlayerIds.contains(playerId));
    }

    public void draw() {
        this.app.image(kinect.getColorImage(), 0, 0, this.app.width, this.app.height);

        ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();
        removeUntrackedPlayers(skeletonArray);

        for (KSkeleton skeleton : skeletonArray) {
            if (skeleton.isTracked()) {
                int playerID = skeleton.getIndexColor();
                KJoint[] kJoints = skeleton.getJoints();

                int col = skeleton.getIndexColor();
                this.app.fill(col);
                this.app.stroke(col);

                PVector spineShoulder = mapCoordinates(kJoints[KinectPV2.JointType_SpineShoulder].getX(), kJoints[KinectPV2.JointType_SpineShoulder].getY(), kJoints[KinectPV2.JointType_SpineShoulder].getZ());
                PVector handLeft = mapCoordinates(kJoints[KinectPV2.JointType_HandLeft].getX(), kJoints[KinectPV2.JointType_HandLeft].getY(), kJoints[KinectPV2.JointType_HandLeft].getZ());
                PVector handRight = mapCoordinates(kJoints[KinectPV2.JointType_HandRight].getX(), kJoints[KinectPV2.JointType_HandRight].getY(), kJoints[KinectPV2.JointType_HandRight].getZ());
                PVector shoulderLeft = mapCoordinates(kJoints[KinectPV2.JointType_ShoulderLeft].getX(), kJoints[KinectPV2.JointType_ShoulderLeft].getY(), kJoints[KinectPV2.JointType_ShoulderLeft].getZ());
                PVector shoulderRight = mapCoordinates(kJoints[KinectPV2.JointType_ShoulderRight].getX(), kJoints[KinectPV2.JointType_ShoulderRight].getY(), kJoints[KinectPV2.JointType_ShoulderRight].getZ());

                if (!Float.isNaN(spineShoulder.x) && !Float.isNaN(handLeft.x) && !Float.isNaN(handRight.x) && !Float.isNaN(shoulderLeft.x) && !Float.isNaN(shoulderRight.x)) {
                    this.joints.drawJoint(spineShoulder);
                    this.joints.drawLine(spineShoulder, handLeft);
                    this.joints.drawLine(spineShoulder, handRight);
                    this.joints.drawLine(shoulderLeft, shoulderRight);
                    this.joints.drawHandPoint(handLeft, kJoints[KinectPV2.JointType_HandLeft].getState());
                    this.joints.drawHandPoint(handRight, kJoints[KinectPV2.JointType_HandRight].getState());


                    if (playerDistances.containsKey(playerID)) {
                        float distance = playerDistances.get(playerID);

                        this.joints.drawBodySpace(spineShoulder, shoulderRight, shoulderLeft, distance);

                        this.app.fill(255);
                        this.app.text("Distance: " + distance, spineShoulder.x, spineShoulder.y - 20);
                    }
                }
            }
        }
    }

    public void showFPS() {
        this.app.text("FPS: " + frameRate, 50, 50);
    }

}
