/**
 * Project: ICON Lab - Kinect Hand to Shoulder Tracking
 * Author: Eduardo Monteiro @ ICON LAB
 * License: MIT License
 */

package com.icon.chick.utils.kinect;

import com.icon.chick.App;
import com.icon.chick.utils.processing.Screen;
import processing.core.*;
import KinectPV2.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Kinect class for handling Kinect sensor operations and hand tracking.
 */
public class Kinect extends PApplet {
    private final App app;
    private final KinectPV2 kinect;
    private final CoordinateMapper coordinateMapper;
    private final Joints joints;
    private final Screen screen;

    private final Boolean isInitialized;
    private Boolean isCalibrating = false;
    private Boolean needCalibration = true;

    private final Map<Integer, Float> playerDistances = new HashMap<>();
    private final Map<Integer, PVector> leftHandPositions = new HashMap<>();
    private final Map<Integer, PVector> rightHandPositions = new HashMap<>();
    private final Map<Integer, ArrayDeque<PVector>> leftHandHistory = new HashMap<>();
    private final Map<Integer, ArrayDeque<PVector>> rightHandHistory = new HashMap<>();

    private static final int SMOOTHING_WINDOW = 5;

    /**
     * Constructor for the Kinect class.
     *
     * @param app The main application instance.
     */
    public Kinect(App app) {
        this.app = app;
        this.joints = new Joints(app);
        this.kinect = new KinectPV2(this.app);
        this.screen = new Screen(this.app);
        this.coordinateMapper = new CoordinateMapper(this.app, this.kinect);

        this.kinect.enableSkeletonColorMap(true);
        this.kinect.enableColorImg(true);

        this.kinect.init();
        this.isInitialized = true;
    }

    /**
     * Draw method to render the Kinect color image and process skeleton data.
     */
    public void draw() {
        this.app.image(kinect.getColorImage(), 0, 0, this.app.width, this.app.height);

        ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();
        if (skeletonArray.isEmpty() && !this.isCalibrating) {
            screen.displayMessage("No skeletons detected");
            return;
        }

        removeUntrackedPlayers(skeletonArray);

        this.run();
        // this.debugVisuals(skeletonArray);
    }

    /**
     * Main processing method to handle hand positions.
     */
    public void run() {
        Map<Integer, MappedCoordinates[]> handPositions = getHandPositions();
        for (Map.Entry<Integer, MappedCoordinates[]> entry : handPositions.entrySet()) {
            int playerID = entry.getKey();
            MappedCoordinates[] hands = entry.getValue();

            MappedCoordinates mappedLeftHand = hands[0];
            MappedCoordinates mappedRightHand = hands[1];

            System.out.println("Player: " + playerID + " | LEFT: " + mappedLeftHand.corrected + " | RIGHT: " + mappedRightHand.corrected);
        }
    }

    /**
     * Debug method to visualize skeleton data.
     * @param skeletonArray the list of skeletons to visualize.
     */
    private void debugVisuals(ArrayList<KSkeleton> skeletonArray) {
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

                PVector spineShoulder = coordinateMapper.mapCoordinates(kJoints[KinectPV2.JointType_SpineShoulder]);
                PVector shoulderLeft = coordinateMapper.mapCoordinates(kJoints[KinectPV2.JointType_ShoulderLeft]);
                PVector shoulderRight = coordinateMapper.mapCoordinates(kJoints[KinectPV2.JointType_ShoulderRight]);

                if (!Float.isNaN(spineShoulder.x) && !Float.isNaN(shoulderLeft.x) && !Float.isNaN(shoulderRight.x)) {
                    this.joints.drawJoint(spineShoulder);
                    this.joints.drawLine(shoulderLeft, shoulderRight);

                    float originalShoulderDistance = PVector.dist(shoulderLeft, shoulderRight);
                    float betterShoulderDistance = originalShoulderDistance * 1.2f;

                    this.joints.drawJoint(shoulderLeft);
                    this.joints.drawBox(shoulderLeft, betterShoulderDistance);
                    this.joints.drawJoint(shoulderRight);
                    this.joints.drawBox(shoulderRight, betterShoulderDistance);

                    updateHandPositions(playerID, kJoints);

                    PVector smoothedLeftHand = leftHandPositions.get(playerID);
                    PVector smoothedRightHand = rightHandPositions.get(playerID);

                    if (smoothedLeftHand != null && smoothedRightHand != null) {
                        MappedCoordinates mappedLeftHand = coordinateMapper.mapToBox(smoothedLeftHand, shoulderLeft, betterShoulderDistance);
                        MappedCoordinates mappedRightHand = coordinateMapper.mapToBox(smoothedRightHand, shoulderRight, betterShoulderDistance);

                        this.app.fill(0, 255, 0);
                        this.app.ellipse(mappedRightHand.corrected.x, mappedLeftHand.corrected.y, 20, 20);
                        this.app.fill(0, 0, 255);
                        this.app.ellipse(mappedRightHand.corrected.x, mappedRightHand.corrected.y, 20, 20);

                        this.app.fill(255);
                        this.app.text("Left Hand: " + mappedLeftHand.corrected, smoothedLeftHand.x, smoothedLeftHand.y - 20);
                        this.app.text("Right Hand: " + mappedRightHand.corrected, smoothedRightHand.x, smoothedRightHand.y - 20);

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

    /**
     * Retrieves the hand positions for tracked skeletons.
     *
     * @return A map of player IDs to their corresponding hand positions.
     */
    public Map<Integer, MappedCoordinates[]> getHandPositions() {
        return kinect.getSkeletonColorMap().parallelStream()
                .filter(KSkeleton::isTracked)
                .collect(Collectors.toMap(
                        KSkeleton::getIndexColor,
                        skeleton -> {
                            int playerID = skeleton.getIndexColor();
                            KJoint[] kJoints = skeleton.getJoints();

                            updateHandPositions(playerID, kJoints);

                            PVector shoulderLeft = coordinateMapper.mapCoordinates(kJoints[KinectPV2.JointType_ShoulderLeft]);
                            PVector shoulderRight = coordinateMapper.mapCoordinates(kJoints[KinectPV2.JointType_ShoulderRight]);
                            float shoulderDistance = PVector.dist(shoulderLeft, shoulderRight) * 1.2f;

                            MappedCoordinates mappedLeftHand = coordinateMapper.mapToBox(leftHandPositions.get(playerID), shoulderLeft, shoulderDistance);
                            MappedCoordinates mappedRightHand = coordinateMapper.mapToBox(rightHandPositions.get(playerID), shoulderRight, shoulderDistance);

                            return new MappedCoordinates[]{mappedLeftHand, mappedRightHand};
                        }
                ));
    }

    /**
     * Calibrates the Kinect sensor for the current environment.
     */
    public void calibrate() {
    this.isCalibrating = true;
    System.out.println("CALIBRATING");
    screen.displayMessage("CALIBRATING...");

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

            screen.displayMessage("CALIBRATED PLAYER " + playerId + " WITH DISTANCE " + averageDistance);
            System.out.println("CALIBRATED PLAYER " + playerId + " WITH DISTANCE " + averageDistance);
        }
    }

        screen.displayMessage("CALIBRATION COMPLETE");
    System.out.println("CALIBRATION COMPLETE");
    this.isCalibrating = false;
}

    /**
     * Removes players that are no longer tracked.
     *
     * @param trackedSkeletons The list of currently tracked skeletons.
     */
    private void removeUntrackedPlayers(ArrayList<KSkeleton> trackedSkeletons) {
        Set<Integer> trackedPlayerIds = new HashSet<>();
        for (KSkeleton skeleton : trackedSkeletons) {
            if (skeleton.isTracked()) {
                trackedPlayerIds.add(skeleton.getIndexColor());
            }
        }
        playerDistances.keySet().removeIf(playerId -> !trackedPlayerIds.contains(playerId));
    }

    /**
     * Updates the hand positions for a given player.
     *
     * @param playerID The ID of the player.
     * @param kJoints The array of joints for the player.
     */
    private void updateHandPositions(int playerID, KJoint[] kJoints) {
        PVector handLeft = coordinateMapper.mapCoordinates(kJoints[KinectPV2.JointType_HandLeft]);
        PVector handRight = coordinateMapper.mapCoordinates(kJoints[KinectPV2.JointType_HandRight]);

        leftHandHistory.computeIfAbsent(playerID, k -> new ArrayDeque<>()).add(handLeft);
        rightHandHistory.computeIfAbsent(playerID, k -> new ArrayDeque<>()).add(handRight);

        if (leftHandHistory.get(playerID).size() > SMOOTHING_WINDOW) {
            leftHandHistory.get(playerID).poll();
        }

        if (rightHandHistory.get(playerID).size() > SMOOTHING_WINDOW) {
            rightHandHistory.get(playerID).poll();
        }

        PVector smoothedLeftHand = coordinateMapper.smoothHandPositions(new ArrayList<>(leftHandHistory.get(playerID)));
        PVector smoothedRightHand = coordinateMapper.smoothHandPositions(new ArrayList<>(rightHandHistory.get(playerID)));

        leftHandPositions.put(playerID, smoothedLeftHand);
        rightHandPositions.put(playerID, smoothedRightHand);
    }
}
