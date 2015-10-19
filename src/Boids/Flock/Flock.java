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
    public float neighborDist = 40;
    public float separation   = 10f;

    public float separationFactor = 10.0f;
    public float alignmentFactor  = 1f;
    public float cohesionFactor   = 1f;

    public float viewAngle = 120;

    public float x, y;

    public float maxSpeed  = 2f;
    public float walkSpeed = 1f;
    public float maxSteer  = .2f;

    public float speedBurst      = 0;
    public float sizeBurst       = 0;
    public float separationBurst = 0;
    public float colorBurst      = 0;
    public int colorBurstStep;

    public int color;
    public int red;
    public int green;
    public int blue;

    public float size = 2f;

    public Flock(PApplet pa) {
        this.pa = pa;
        x = this.pa.width / 2;
        y = this.pa.height / 2;
        boids = new ArrayList<>(); // Initialize the ArrayList
        Random r = new Random();
        red =r.nextInt(255);
        green =r.nextInt(255);
        blue =r.nextInt(255);
        colorBurstStep = 10;

    }

    public void run() {
        for (Boid b : boids) {
            b.run(this);  // Passing the entire list of boids to each boid individually
        }
        normalize();
    }

    public void addBoid(Boid b) {
        boids.add(b);
    }

    public void addBoid() {
        boids.add(new Boid(this.pa, this.x, this.y));
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

    public float getWalkSpeed() {
        return walkSpeed + speedBurst;
    }

    public float getMaxSpeed() {
        return maxSpeed + speedBurst;
    }

    public void normalize() {
        if (speedBurst > 0) {
            speedBurst -= .05f;
        } else if (speedBurst < 0) {
            speedBurst += .05f;
        }
        if (sizeBurst > 0) {
            sizeBurst -= .05f;
        } else if (sizeBurst < 0) {
            sizeBurst += .05f;
        }
        if (separationBurst > 0) {
            separationBurst -= .5f;
        } else if (separationBurst < 0) {
            separationBurst += .5f;
        }
        if (colorBurst > 0) {
            colorBurst -= .5f;
        } else if (colorBurst < 0) {
            colorBurst += .5f;
        }
    }

    public float getSize() {
        return size + sizeBurst;
    }

    public float getSeparation() {
        return separation + separationBurst + getSize();
    }

    public int getColor() {
        return  pa.color(red + colorBurst * colorBurstStep,green + colorBurst * colorBurstStep,blue + colorBurst * colorBurstStep);
    }
}
