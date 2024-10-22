package com.icon;// ICON LAB 2024

import processing.core.*;

public class App extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[] { "com.icon.App" });
    }

    // How to use a class outside the main
    Kinect kinect;

    @Override
    public void settings() {
        // change it to actual screen size
        size(1680, 1050, P3D);
    }

    @Override
    public void setup() {
        kinect = new Kinect(this);
        kinect.calibrate();
    }

    @Override
    public void draw() {
        background(0);
        kinect.draw();
        kinect.showFPS();
    }
}