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
        size(1920, 1080, P3D);
    }

    @Override
    public void setup() {
        kinect = new Kinect(this);
    }

    @Override
    public void draw() {
        background(0);
        kinect.draw();
        kinect.showFPS();
    }
}