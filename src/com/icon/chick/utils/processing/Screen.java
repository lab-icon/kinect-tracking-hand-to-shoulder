package com.icon.chick.utils.processing;

import processing.core.PApplet;
import com.icon.chick.App;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * The Screen class provides methods to display frames per second (FPS) and messages on the screen.
 */
public class Screen extends PApplet {
    App app;

    /**
     * Constructor for the Screen class.
     *
     * @param app The main application instance.
     */
    public Screen(App app) {
        this.app = app;
    }

    /**
     * Displays the current frames per second (FPS) on the screen.
     */
    public void showFPS() {
        this.app.text("FPS: " + frameRate, 50, 50);
    }

    /**
     * Displays a message on the screen.
     *
     * @param message The message to display.
     */
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