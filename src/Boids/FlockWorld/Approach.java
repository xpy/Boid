package Boids.FlockWorld;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Approach
 * Created by xpy on 29-Oct-15.
 */
public class Approach {
    public PApplet pa;
    public float   power;
    public float   range;

    public PVector location = new PVector();

    public Approach(PApplet pa, float power, float range, float x, float y) {
        this.pa = pa;
        this.power = power;
        this.range = range;
        location.x = x;
        location.y = y;
    }

    public void run() {
        render();
    }

    public void render() {
        pa.noFill();
        pa.stroke(200);
        pa.ellipse(location.x, location.y,2*range,2* range);
    }
}
