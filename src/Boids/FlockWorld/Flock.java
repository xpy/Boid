package Boids.FlockWorld;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Flock
 * Created by xpy on 17-Oct-15.
 */
public class Flock {

    public ArrayList<Boid> boids; // An ArrayList for all the boids
    public ArrayList<Avoid> avoids; // An ArrayList for all the boids
    public ArrayList<Approach> approaches; // An ArrayList for all the boids

    protected PApplet pa;

    // The max distance to check
    public float neighborDist = 50;
    public float separation   = 20f;

    public float separationFactor = 1.00f;
    public float alignmentFactor  = 1f;
    public float cohesionFactor   = 1f;

    public float viewAngle = 160;

    public float x, y;

    public float maxSpeed  = 2f;
    public float walkSpeed = 1f;
    public float maxSteer  = .2f;

    public float speedBurst      = 0;
    public float sizeBurst       = 0;
    public float separationBurst = 0;
    public float colorBurst      = 0;
    public int colorBurstStep;

    public float speedBurstReduce      = .05f;
    public float sizeBurstReduce       = .05f;
    public float separationBurstReduce = .5f;
    public float colorBurstReduce      = .5f;

    public int red;
    public int green;
    public int blue;

    public float size = 2f;

    public boolean absoluteSeparation = false;

    public boolean checkVision = false;

    public Flock(PApplet pa) {
        this.pa = pa;
        x = this.pa.width / 2;
        y = this.pa.height / 2;
        boids = new ArrayList<>(); // Initialize the ArrayList
        avoids = new ArrayList<>(); // Initialize the ArrayList
        approaches = new ArrayList<>(); // Initialize the ArrayList
        Random r = new Random();
        red = r.nextInt(255);
        green = r.nextInt(255);
        blue = r.nextInt(255);
        colorBurstStep = 10;

    }

    public void run(World fw) {

        for (Boid b : boids) {
            b.run(fw, this);  // Passing the entire list of boids to each boid individually
        }
        for (Avoid a : avoids) {
            a.run();  // Passing the entire list of boids to each boid individually
        }
        for (Approach a : approaches) {
            a.run();  // Passing the entire list of boids to each boid individually
        }
        normalize();
    }

    public void addBoid(Boid b) {
        boids.add(b);
    }

    public void addBoid() {
        boids.add(new Boid(this.pa, this.x, this.y));
    }

    public void addAvoid(Avoid avoid) {
        avoids.add(avoid);

    }

    public void addApproach(Approach approach) {
        approaches.add(approach);

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

    public float getSize() {
        return size + sizeBurst;
    }

    public float getSeparation() {
        return separation + separationBurst + getSize();
    }

    public int getColor() {
        return pa.color(red + colorBurst * colorBurstStep, green + colorBurst * colorBurstStep, blue + colorBurst * colorBurstStep);
    }

    public void normalize() {
        if (speedBurst > 0) {
            speedBurst -= speedBurstReduce;
        } else if (speedBurst < 0) {
            speedBurst += speedBurstReduce;
        }
        if (sizeBurst > 0) {
            sizeBurst -= sizeBurstReduce;
        } else if (sizeBurst < 0) {
            sizeBurst += sizeBurstReduce;
        }
        if (separationBurst > 0) {
            separationBurst -= separationBurstReduce;
        } else if (separationBurst < 0) {
            separationBurst += separationBurstReduce;
        }
        if (colorBurst > 0) {
            colorBurst -= colorBurstReduce;
        } else if (colorBurst < 0) {
            colorBurst += colorBurstReduce;
        }
    }

    public List<Boid> getBoids() {
        return boids;
    }
}
