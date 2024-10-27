package com.icon.chick.utils.kinect;

import com.icon.chick.App;
import processing.core.*;
import KinectPV2.*;

import java.util.*;

public class Kinect extends PApplet {
    private final App app;
    private final KinectPV2 kinect;
    private final Joints joints;
    private final Boolean isInitialized;
    private Boolean isCalibrating = false;
    private Boolean needCalibration = true;

    private final Map<Integer, Float> playerDistances = new HashMap<>();
    private final Map<Integer, PVector> leftHandPositions = new HashMap<>();
    private final Map<Integer, PVector> rightHandPositions = new HashMap<>();
    private final Map<Integer, List<PVector>> leftHandHistory = new HashMap<>();
    private final Map<Integer, List<PVector>> rightHandHistory = new HashMap<>();

    private static final int SMOOTHING_WINDOW = 5;

    public Kinect(App app) {
        this.app = app;
        this.joints = new Joints(app);
        this.kinect = new KinectPV2(this.app);

        this.kinect.enableSkeletonColorMap(true);
        this.kinect.enableColorImg(true);

        this.kinect.init();
        this.isInitialized = true;
    }

    public void draw() {
        this.app.image(kinect.getColorImage(), 0, 0, this.app.width, this.app.height);

        ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();
        if (skeletonArray.isEmpty() && !this.isCalibrating) {
            displayMessage("No skeletons detected");
            return;
        }

        removeUntrackedPlayers(skeletonArray);

        for (KSkeleton skeleton : skeletonArray) {
            if (skeleton.isTracked()) {
                if (this.needCalibration && this.isInitialized) {
                    this.calibrate();
                    this.needCalibration = false;
                }

                int playerID = skeleton.getIndexColor();
                KJoint[] kJoints = skeleton.getJoints();

                this.app.fill(playerID);
                this.app.stroke(playerID);

                PVector spineShoulder = mapCoordinates(kJoints[KinectPV2.JointType_SpineShoulder]);
                PVector shoulderLeft = mapCoordinates(kJoints[KinectPV2.JointType_ShoulderLeft]);
                PVector shoulderRight = mapCoordinates(kJoints[KinectPV2.JointType_ShoulderRight]);

                if (!Float.isNaN(spineShoulder.x) && !Float.isNaN(shoulderLeft.x) && !Float.isNaN(shoulderRight.x)) {
                    this.joints.drawJoint(spineShoulder);
                    this.joints.drawLine(shoulderLeft, shoulderRight);

                    float originalShoulderDistance = PVector.dist(shoulderLeft, shoulderRight);
                    float betterShoulderDistance = originalShoulderDistance * 1.2f;

                    this.joints.drawJoint(shoulderLeft);
                    this.joints.drawBox(shoulderLeft, betterShoulderDistance);
                    this.joints.drawJoint(shoulderRight);
                    this.joints.drawBox(shoulderRight, betterShoulderDistance);

                    updateHandPositions(skeleton);

                    PVector smoothedLeftHand = leftHandPositions.get(playerID);
                    PVector smoothedRightHand = rightHandPositions.get(playerID);

                    if (smoothedLeftHand != null && smoothedRightHand != null) {
                        PVector mappedLeftHand = mapToBox(smoothedLeftHand, shoulderLeft, betterShoulderDistance);
                        PVector mappedRightHand = mapToBox(smoothedRightHand, shoulderRight, betterShoulderDistance);

                        this.app.fill(0, 255, 0);
                        this.app.ellipse(mappedLeftHand.x, mappedLeftHand.y, 20, 20);
                        this.app.fill(0, 0, 255);
                        this.app.ellipse(mappedRightHand.x, mappedRightHand.y, 20, 20);


                        this.app.fill(255);
                        this.app.text("Left Hand: " + mappedLeftHand, smoothedLeftHand.x, smoothedLeftHand.y - 20);
                        this.app.text("Right Hand: " + mappedRightHand, smoothedRightHand.x, smoothedRightHand.y - 20);

                        if (playerDistances.containsKey(playerID)) {
                            float distance = playerDistances.get(playerID);
                            this.joints.drawBodySpace(spineShoulder, shoulderRight, shoulderLeft, distance);
                            this.app.text("Distance: " + distance, spineShoulder.x, spineShoulder.y - 20);
                        }
                    }
                }
            }
        }
    }

    private PVector mapCoordinates(KJoint joint) {
        float x = joint.getX(), y = joint.getY(), z = joint.getZ();
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
    this.isCalibrating = true;
    System.out.println("CALIBRATING");
    displayMessage("CALIBRATING...");

    ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();
    System.out.println("Number of skeletons on calibration: " + skeletonArray.size());
    for (KSkeleton skeleton : skeletonArray) {
        if (skeleton.isTracked()) {
            int playerId = skeleton.getIndexColor();
            KJoint[] kJoints = skeleton.getJoints();

            float totalDistance = 0;
            for (int i = 0; i < 10; i++) {
                totalDistance += this.joints.calcJointDistance(kJoints[KinectPV2.JointType_ShoulderRight], kJoints[KinectPV2.JointType_HandRight]);
                System.out.println("Measurement " + (i + 1) + ": " + totalDistance);
            }

            float averageDistance = totalDistance / 10;
            playerDistances.put(playerId, averageDistance);

            displayMessage("CALIBRATED PLAYER " + playerId + " WITH DISTANCE " + averageDistance);
            System.out.println("CALIBRATED PLAYER " + playerId + " WITH DISTANCE " + averageDistance);
        }
    }

    displayMessage("CALIBRATION COMPLETE");
    System.out.println("CALIBRATION COMPLETE");
    this.isCalibrating = false;
}

    private void removeUntrackedPlayers(ArrayList<KSkeleton> trackedSkeletons) {
        Set<Integer> trackedPlayerIds = new HashSet<>();
        for (KSkeleton skeleton : trackedSkeletons) {
            if (skeleton.isTracked()) {
                trackedPlayerIds.add(skeleton.getIndexColor());
            }
        }
        playerDistances.keySet().removeIf(playerId -> !trackedPlayerIds.contains(playerId));
    }

    private PVector mapToBox(PVector hand, PVector shoulder, float shoulderDistance) {
        PVector handRelative = hand.copy().sub(shoulder);
        float mappedX = map(handRelative.x, -shoulderDistance, shoulderDistance, -1, 1);
        float mappedY = map(handRelative.y, -shoulderDistance, shoulderDistance, -1, 1);
        return new PVector(mappedX, mappedY);
    }

    private void updateHandPositions(KSkeleton skeleton) {
        int playerID = skeleton.getIndexColor();
        KJoint[] kJoints = skeleton.getJoints();

        PVector handLeft = mapCoordinates(kJoints[KinectPV2.JointType_HandLeft]);
        PVector handRight = mapCoordinates(kJoints[KinectPV2.JointType_HandRight]);

        leftHandHistory.computeIfAbsent(playerID, k -> new ArrayList<>()).add(handLeft);
        rightHandHistory.computeIfAbsent(playerID, k -> new ArrayList<>()).add(handRight);

        if (leftHandHistory.get(playerID).size() > SMOOTHING_WINDOW) {
            leftHandHistory.get(playerID).removeFirst();
        }

        if (rightHandHistory.get(playerID).size() > SMOOTHING_WINDOW) {
            rightHandHistory.get(playerID).removeFirst();
        }

        PVector smoothedLeftHand = smoothHandPositions(leftHandHistory.get(playerID));
        PVector smoothedRightHand = smoothHandPositions(rightHandHistory.get(playerID));

        leftHandPositions.put(playerID, smoothedLeftHand);
        rightHandPositions.put(playerID, smoothedRightHand);
    }

    private PVector smoothHandPositions(List<PVector> handHistory) {
        float sumX = 0, sumY = 0, sumZ = 0;
        int count = handHistory.size();
        for (PVector pos : handHistory) {
            sumX += pos.x;
            sumY += pos.y;
            sumZ += pos.z;
        }

        return new PVector(sumX / count, sumY / count, sumZ / count);
    }

    private void displayMessage(String message) {
        PGraphics pg = this.app.createGraphics(this.app.width, this.app.height);
        pg.beginDraw();
        pg.background(0);
        pg.textAlign(PConstants.CENTER, PConstants.CENTER);
        pg.textSize(32);
        pg.fill(255);
        pg.text(message, this.app.width / 2f, this.app.height / 2f);
        pg.endDraw();

        this.app.image(pg, 0, 0);
        this.app.redraw();
    }
}
