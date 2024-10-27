package com.icon.chick;// ICON LAB 2024

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
}