package Boids.Flock;

import Boids.Boid.Boid;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Random;

/**
 * Flock
 * Created by xpy on 17-Oct-15.
 */
public class Flock {

    public  ArrayList<Boid> boids; // An ArrayList for all the boids
    private PApplet         pa;

    // The max distance to check
    public float neighborDist = 50;
    public float separation   = 20f;

    public float separationFactor = 1.0f;
    public float alignmentFactor = 1.0f;
    public float cohesionFactor = 1.0f;

    public float viewAngle = 360;

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

    public PVector getCenter() {
        PVector sum   = new PVector(0, 0);   // Start with empty vector to accumulate all locations
        int     count = 0;
        for (Boid other : boids) {
            sum.add(other.location); // Add location
            count++;
        }

        if (count > 0) {
            sum.div(count);
            return sum;
        } else {
            return new PVector(0, 0);
        }
    }


}
