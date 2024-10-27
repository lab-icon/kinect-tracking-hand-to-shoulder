package com.icon.chick.utils.processing;

import processing.core.PApplet;
import com.icon.chick.App;
import processing.core.PConstants;
import processing.core.PGraphics;

public class Screen extends PApplet {
    App app;

    public Screen(App app) {
        this.app = app;
    }

    public void showFPS() {
        this.app.text("FPS: " + frameRate, 50, 50);
    }

    public void displayMessage(String message) {
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