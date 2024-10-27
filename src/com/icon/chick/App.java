package com.icon.chick;// ICON LAB 2024

import com.icon.chick.utils.kinect.Kinect;
import com.icon.chick.utils.processing.Screen;
import processing.core.*;

public class App extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[] { "com.icon.chick.App" });
    }

    // How to use a class outside the main
    Kinect kinect;
    Screen screen = new Screen(this);
    Boolean needsCalibration = true;

    @Override
    public void settings() {
        // change it to actual screen size
        size((int) (1920*0.9), (int) (1080*0.9), P3D);
    }

    @Override
    public void setup() {
        kinect = new Kinect(this);
    }

    @Override
    public void draw() {
        background(0);
        kinect.draw();
        screen.showFPS();
    }
}