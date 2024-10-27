package com.icon.chick.utils.processing;

import processing.core.PApplet;
import com.icon.chick.App;

public class Screen extends PApplet {
    App app;

    public Screen(App app) {
        this.app = app;
    }

    public void showFPS() {
        this.app.text("FPS: " + frameRate, 50, 50);
    }
}