// ICON LAB 2024

import processing.core.*;

public class App extends PApplet {

    public static void main(String[] args) {
        PApplet.main(new String[] { "App" });
    }

    // How to use a class outside the main
    Dot dot = new Dot(this);

    @Override
    public void settings() {
        fullScreen();
    }

    @Override
    public void setup() {
    }

    @Override
    public void draw() {
        background(0);
        dot.draw();
    }
}