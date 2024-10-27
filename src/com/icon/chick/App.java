/**
 * Project: ICON Lab - Kinect Hand to Shoulder Tracking
 * Author: Eduardo Monteiro @ ICON LAB
 * License: MIT License
 */

package com.icon.chick;

import com.icon.chick.utils.kinect.Kinect;
import com.icon.chick.utils.processing.Screen;
import processing.core.*;

public class App extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[] { "com.icon.chick.App" });
    }

    Kinect kinect;
    Screen screen = new Screen(this);

    @Override
    public void settings() {
        // change it to actual screen size
        this.size((int) (1920*0.9), (int) (1080*0.9), PConstants.P3D);
    }

    @Override
    public void setup() {
        this.kinect = new Kinect(this);
    }

    @Override
    public void draw() {
        this.background(0);
        this.kinect.draw();
        this.screen.showFPS();
    }

    @Override
    public void keyPressed() {
        if (this.key == PConstants.CODED) {
            if (this.keyCode == PConstants.SHIFT) {
                this.kinect.calibrate();
            }
        }
    }
}