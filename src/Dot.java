import processing.core.*;

public class Dot extends PApplet {

    private final App app;

    public Dot(App app) {
        this.app = app;
    }

    public void draw() {
        app.background(255);
    }

}
