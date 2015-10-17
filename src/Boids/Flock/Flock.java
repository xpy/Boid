package Boids.Flock;

import Boids.Boid.Boid;
import processing.core.PApplet;

import java.util.ArrayList;

/**
 * Flock
 * Created by xpy on 17-Oct-15.
 */
public class Flock {

    public ArrayList<Boid> boids; // An ArrayList for all the boids
    private PApplet pa;

    public Flock(PApplet pa) {
        this.pa = pa;
        boids = new ArrayList<>(); // Initialize the ArrayList
    }

    public void run() {
        for (Boid b : boids) {
            b.run(this);  // Passing the entire list of boids to each boid individually
        }
    }

    public void addBoid(Boid b) {
        boids.add(b);
    }


}
