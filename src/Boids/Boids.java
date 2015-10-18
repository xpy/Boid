package Boids;

import Boids.Boid.Boid;
import Boids.Flock.Flock;
import processing.core.PApplet;

/**
 * Boids
 * Created by xpy on 17-Oct-15.
 */
public class Boids extends PApplet {

    Flock flock;

    public void setup() {
        size(640, 360);
        flock = new Flock(this);
        // Add an initial set of boids into the system
        for (int i = 0; i < 200; i++) {
            flock.addBoid(new Boid(this, width / 2, height / 2));
        }


// Add a new boid into the System


    }


    public void draw() {

//        background(50);
        fill(0,0,0,10);
        rect(0,0,width,height);
        flock.run();
        noFill();
        stroke(255,100);
ellipse(mouseX,mouseY,200,200);
    }

    public void mousePressed() {
        flock.addBoid(new Boid(this, mouseX, mouseY));
//        flock.separation++;
    }
}
