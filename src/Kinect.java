import org.jetbrains.annotations.NotNull;
import processing.core.*;
import KinectPV2.*;

import java.util.ArrayList;

public class Kinect extends PApplet {

    private final App app;

    KinectPV2 kinect;

    public Kinect(App app) {
        this.app = app;
        kinect = new KinectPV2(this.app);

        kinect.enableSkeletonColorMap(true);
        kinect.enableColorImg(true);

        kinect.init();
    }

    public void draw() {
        this.app.image(kinect.getColorImage(), 0, 0, this.app.width, this.app.height);

        ArrayList<KSkeleton> skeletonArray = kinect.getSkeletonColorMap();
        for (KSkeleton skeleton : skeletonArray) {
            if (skeleton.isTracked()) {
                KJoint[] joints = skeleton.getJoints();

                int col = skeleton.getIndexColor();
                this.app.fill(col);
                this.app.stroke(col);
                drawBodyPoint(joints);

                drawLine(joints[KinectPV2.JointType_SpineShoulder], joints[KinectPV2.JointType_HandLeft]);
                drawLine(joints[KinectPV2.JointType_SpineShoulder], joints[KinectPV2.JointType_HandRight]);

                drawHandPoint(joints[KinectPV2.JointType_HandLeft]);
                drawHandPoint(joints[KinectPV2.JointType_HandRight]);

                drawBodySpace(joints[KinectPV2.JointType_SpineShoulder]);
            }
        }
    }

    public void showFPS() {
        this.app.text("FPS: " + frameRate, 50, 50);
    }

    private void drawBodyPoint(KJoint[] joints) {
        drawJoint(joints, KinectPV2.JointType_SpineShoulder);
    }

    private void drawJoint(KJoint[] joints, int jointType) {
        this.app.pushMatrix();
        this.app.translate(joints[jointType].getX(), joints[jointType].getY(), joints[jointType].getZ());
        this.app.ellipse(0, 0, 25, 25);
        this.app.popMatrix();
    }

    private void drawLine(KJoint joint1, KJoint joint2) {
        this.app.pushMatrix();
        this.app.fill(0,0,255);
        this.app.line(joint1.getX(), joint1.getY(), joint1.getZ(), joint2.getX(), joint2.getY(), joint2.getZ());
        this.app.popMatrix();
    }

    private void drawBodySpace(@NotNull KJoint joint){
        this.app.pushMatrix();
        this.app.fill(0, 0, 225);
        this.app.translate(joint.getX(), joint.getY());
        this.app.rectMode(CENTER);
        this.app.rect(0, 0,200,200);
        this.app.popMatrix();
    }

    private float calcJointDistance(@NotNull KJoint joint1, @NotNull KJoint joint2) {
        return dist(joint1.getX(), joint1.getY(), joint2.getX(), joint2.getX());
    }

    private void drawHandPoint(@NotNull KJoint joint) {
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
