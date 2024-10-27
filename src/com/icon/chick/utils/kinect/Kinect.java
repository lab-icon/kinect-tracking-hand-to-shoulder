package com.icon.chick.utils.kinect;

import com.icon.chick.App;
import processing.core.*;
import KinectPV2.*;

import java.lang.reflect.Array;
import java.util.*;

public class Kinect extends PApplet {
    private final App app;
    private final KinectPV2 kinect;
    private final Joints joints;
    private Boolean isInitialized;
    private Boolean needCalibration = true;

    private final Map<Integer, Float> playerDistances;
    private final Map<Integer, PVector> leftHandPositions;
    private final Map<Integer, PVector> rightHandPositions;
    private final Map<Integer, List<PVector>> leftHandHistory;
    private final Map<Integer, List<PVector>> rightHandHistory;

    private static final int SMOOTHING_WINDOW = 5;

    public Kinect(App app) {
        this.app = app;
        this.joints = new Joints(app);

        this.playerDistances = new HashMap<>();
        this.leftHandPositions = new HashMap<>();
        this.rightHandPositions = new HashMap<>();
        this.leftHandHistory = new HashMap<>();
        this.rightHandHistory = new HashMap<>();

        kinect = new KinectPV2(this.app);

        kinect.enableSkeletonColorMap(true);
        kinect.enableColorImg(true);

        kinect.init();
        this.isInitialized = true;
    }

    public void draw() {
        this.app.image(kinect.getColorImage(), 0, 0, this.app.width, this.app.height);

        // Map the skeleton joints to the screen and remove untracked players
        ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();
        System.out.println("Number of skeletons detected initially:" + skeletonArray.size());
        waitForSkeletons(skeletonArray);
        removeUntrackedPlayers(skeletonArray);

        for (KSkeleton skeleton : skeletonArray) {
            if (skeleton.isTracked()) {

                if (this.needCalibration && this.isInitialized) {
                    this.calibrate();
                    this.needCalibration = false;
                }

                // Player data
                int playerID = skeleton.getIndexColor();
                KJoint[] kJoints = skeleton.getJoints();

                this.app.fill(playerID);
                this.app.stroke(playerID);

                PVector spineShoulder = mapCoordinates(kJoints[KinectPV2.JointType_SpineShoulder].getX(), kJoints[KinectPV2.JointType_SpineShoulder].getY(), kJoints[KinectPV2.JointType_SpineShoulder].getZ());
                PVector shoulderLeft = mapCoordinates(kJoints[KinectPV2.JointType_ShoulderLeft].getX(), kJoints[KinectPV2.JointType_ShoulderLeft].getY(), kJoints[KinectPV2.JointType_ShoulderLeft].getZ());
                PVector shoulderRight = mapCoordinates(kJoints[KinectPV2.JointType_ShoulderRight].getX(), kJoints[KinectPV2.JointType_ShoulderRight].getY(), kJoints[KinectPV2.JointType_ShoulderRight].getZ());

                if (!Float.isNaN(spineShoulder.x) && !Float.isNaN(shoulderLeft.x) && !Float.isNaN(shoulderRight.x)) {
                    this.joints.drawJoint(spineShoulder);
                    this.joints.drawLine(shoulderLeft, shoulderRight);

                    float shoulderDistance = PVector.dist(shoulderLeft, shoulderRight);
                    this.joints.drawJoint(shoulderLeft);
                    this.joints.drawBox(shoulderLeft, shoulderDistance);
                    this.joints.drawJoint(shoulderRight);
                    this.joints.drawBox(shoulderRight, shoulderDistance);

                    updateHandPositions(skeleton);

                    // Smoothed hand positions
                    PVector smoothedLeftHand = leftHandPositions.get(playerID);
                    PVector smoothedRightHand = rightHandPositions.get(playerID);

                    if (smoothedLeftHand != null && smoothedRightHand != null) {
                        PVector mappedLeftHand = mapToBox(smoothedLeftHand, shoulderLeft, shoulderDistance);
                        PVector mappedRightHand = mapToBox(smoothedRightHand, shoulderRight, shoulderDistance);

                        this.app.fill(0, 255, 0); // Green for left hand
                        this.app.ellipse(mappedLeftHand.x, mappedLeftHand.y, 20, 20);
                        this.app.fill(0, 0, 255); // Blue for right hand
                        this.app.ellipse(mappedRightHand.x, mappedRightHand.y, 20, 20);


                        this.app.fill(255);
                        this.app.text("Left Hand: " + mappedLeftHand, smoothedLeftHand.x, smoothedLeftHand.y - 20);
                        this.app.text("Right Hand: " + mappedRightHand, smoothedRightHand.x, smoothedRightHand.y - 20);

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
    PGraphics pg = this.app.createGraphics(this.app.width, this.app.height);
    System.out.println("CALIBRATING");

    pg.beginDraw();
    pg.background(0); // Clear the background
    pg.textAlign(CENTER, CENTER);
    pg.textSize(32);
    pg.fill(255);
    pg.text("CALIBRATING...", (float) this.app.width / 2, (float) this.app.height / 2);
    pg.endDraw();

    this.app.image(pg, 0, 0);
    this.app.redraw(); // Force redraw to display the calibration text

    ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();
    System.out.println("Number of skeletons: " + skeletonArray.size());
    for (KSkeleton skeleton : skeletonArray) {
        if (skeleton.isTracked()) {
            int playerId = skeleton.getIndexColor();
            KJoint[] kJoints = skeleton.getJoints();

            int numMeasurements = 10;
            float totalDistance = 0;

            for (int i = 0; i < numMeasurements; i++) {
                float shoulderToHandDistance = this.joints.calcJointDistance(kJoints[KinectPV2.JointType_ShoulderRight], kJoints[KinectPV2.JointType_HandRight]);
                totalDistance += shoulderToHandDistance;
                System.out.println("Measurement " + (i + 1) + ": " + shoulderToHandDistance);
            }

            float averageDistance = totalDistance / numMeasurements;
            playerDistances.put(playerId, averageDistance);

            pg.beginDraw();
            pg.background(0); // Clear the background
            pg.textAlign(CENTER, CENTER);
            pg.textSize(32);
            pg.fill(255);
            pg.text("CALIBRATED: " + averageDistance, (float) this.app.width / 2, (float) this.app.height / 2);
            System.out.println("CALIBRATED: " + averageDistance);
            pg.endDraw();

            this.app.image(pg, 0, 0);
            this.app.redraw(); // Force redraw to display the calibration result
        }
    }

        pg.beginDraw();
        pg.background(0); // Clear the background
        pg.textAlign(CENTER, CENTER);
        pg.textSize(32);
        pg.fill(255);
        pg.text("CALIBRATION COMPLETE", (float) this.app.width / 2, (float) this.app.height / 2);
        System.out.println("CALIBRATION COMPLETE");
        pg.endDraw();

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

        float mappedX = map(handRelative.x, 0, shoulderDistance / 2, -1, 1);
        float mappedY = map(handRelative.y, -shoulderDistance / 2, shoulderDistance / 2, -1, 1);

        return new PVector(mappedX, mappedY);
    }

    private void updateHandPositions(KSkeleton skeleton) {
        int playerID = skeleton.getIndexColor();
        KJoint[] kJoints = skeleton.getJoints();

        PVector handLeft = mapCoordinates(kJoints[KinectPV2.JointType_HandLeft].getX(), kJoints[KinectPV2.JointType_HandLeft].getY(), kJoints[KinectPV2.JointType_HandLeft].getZ());
        PVector handRight = mapCoordinates(kJoints[KinectPV2.JointType_HandRight].getX(), kJoints[KinectPV2.JointType_HandRight].getY(), kJoints[KinectPV2.JointType_HandRight].getZ());

        // Updates the history
        leftHandHistory.computeIfAbsent(playerID, k -> new ArrayList<>()).add(handLeft);
        rightHandHistory.computeIfAbsent(playerID, k -> new ArrayList<>()).add(handRight);

        // Maintain the smoothing window size
        if (leftHandHistory.get(playerID).size() > SMOOTHING_WINDOW) {
            leftHandHistory.get(playerID).removeFirst();
        }

        if (rightHandHistory.get(playerID).size() > SMOOTHING_WINDOW) {
            rightHandHistory.get(playerID).removeFirst();
        }

        // Smooth the hand position
        PVector smoothedLeftHand = smoothHandPositions(leftHandHistory.get(playerID));
        PVector smoothedRightHand = smoothHandPositions(rightHandHistory.get(playerID));

        // Store the smoothed hand positions
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

    private void waitForSkeletons(ArrayList<KSkeleton> skeletonArray) {
        if (skeletonArray.isEmpty()) {
            // Display text if no skeletons are detected
            PGraphics pg = this.app.createGraphics(this.app.width, this.app.height);
            pg.beginDraw();
            pg.background(0); // Clear the background
            pg.textAlign(CENTER, CENTER);
            pg.textSize(32);
            pg.fill(255);
            pg.text("Waiting for skeletons...", (float) this.app.width / 2, (float) this.app.height / 2);
            pg.endDraw();
            this.app.image(pg, 0, 0);
            this.app.redraw(); // Force redraw to display the text
        }
    }
}
