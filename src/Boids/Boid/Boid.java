package Boids.Boid;

import Boids.Flock.Flock;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * Boids
 * Created by xpy on 17-Oct-15.
 */
public class Boid {


    PVector location;
    PVector velocity;
    PVector acceleration;
    float   r;
    float   maxforce;    // Maximum steering force
    float   maxspeed;    // Maximum speed

    private PApplet pa;

    public Boid(PApplet pa, float x, float y) {
        this.pa = pa;
        acceleration = new PVector(0, 0);

        // This is a new PVector method not yet implemented in JS
        // velocity = PVector.random2D();

        // Leaving the code temporarily this way so that this example runs in JS
        float angle = pa.random(PApplet.TWO_PI);
        velocity = new PVector(PApplet.cos(angle), PApplet.sin(angle));

        location = new PVector(x, y);
        r = 2.0f;
        maxspeed = 2;
        maxforce = 0.03f;
    }

    public void run(Flock flock) {
        flock(flock);
        update();
        borders();
        render();
    }

    void applyForce(PVector force) {
        // We could add mass here if we want A = F / M
        acceleration.add(force);
    }

    // We accumulate a new acceleration each time based on three rules
    void flock(Flock flock) {
        PVector sep = separate(flock);   // Separation
        PVector ali = align(flock);      // Alignment
        PVector coh = cohesion(flock);   // Cohesion
        // Arbitrarily weight these forces
        sep.mult(1.5f);
        ali.mult(1.0f);
        coh.mult(1.0f);
        // Add the force vectors to acceleration
        applyForce(sep);
        applyForce(ali);
        applyForce(coh);
    }

    // Method to update location
    void update() {
        // Update velocity
        velocity.add(acceleration);
        // Limit speed
        velocity.limit(maxspeed);
        location.add(velocity);
        // Reset accelertion to 0 each cycle
        acceleration.mult(0);
    }

    // A method that calculates and applies a steering force towards a target
    // STEER = DESIRED MINUS VELOCITY
    PVector seek(PVector target) {
        PVector desired = PVector.sub(target, location);  // A vector pointing from the location to the target
        // Scale to maximum speed
        desired.normalize();
        desired.mult(maxspeed);

        // Above two lines of code below could be condensed with new PVector setMag() method
        // Not using this method until Processing.js catches up
        // desired.setMag(maxspeed);

        // Steering = Desired minus Velocity
        PVector steer = PVector.sub(desired, velocity);
        steer.limit(maxforce);  // Limit to maximum steering force
        return steer;
    }

    public void render() {
        // Draw a triangle rotated in the direction of velocity
        float theta = velocity.heading() + PApplet.radians(90);
        // heading2D() above is now heading() but leaving old syntax until Processing.js catches up

        pa.fill(200, 100);
        pa.stroke(255);
        pa.pushMatrix();
        pa.translate(location.x, location.y);
        pa.rotate(theta);
        pa.beginShape(PApplet.TRIANGLES);
        pa.vertex(0, -r * 2);
        pa.vertex(-r, r * 2);
        pa.vertex(r, r * 2);
        pa.endShape();
        pa.popMatrix();
    }

    // Wraparound
    void borders() {
        if (location.x < -r) location.x = pa.width + r;
        if (location.y < -r) location.y = pa.height + r;
        if (location.x > pa.width + r) location.x = -r;
        if (location.y > pa.height + r) location.y = -r;
    }

    // Separation
    // Method checks for nearby boids and steers away
    PVector separate(Flock flock) {
        float   desiredseparation = 25.0f;
        PVector steer             = new PVector(0, 0, 0);
        int     count             = 0;
        // For every boid in the system, check if it's too close
        for (Boid other : flock.boids) {
            float d = PVector.dist(location, other.location);
            // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
            if ((d > 0) && (d < desiredseparation)) {
                // Calculate vector pointing away from neighbor
                PVector diff = PVector.sub(location, other.location);
                diff.normalize();
                diff.div(d);        // Weight by distance
                steer.add(diff);
                count++;            // Keep track of how many
            }
        }
        // Average -- divide by how many
        if (count > 0) {
            steer.div((float) count);
        }

        // As long as the vector is greater than 0
        if (steer.mag() > 0) {
            // First two lines of code below could be condensed with new PVector setMag() method
            // Not using this method until Processing.js catches up
            // steer.setMag(maxspeed);

            // Implement Reynolds: Steering = Desired - Velocity
            steer.normalize();
            steer.mult(maxspeed);
            steer.sub(velocity);
            steer.limit(maxforce);
        }
        return steer;
    }

    // Alignment
    // For every nearby boid in the system, calculate the average velocity
    PVector align(Flock flock) {
        float   neighbordist = 50;
        PVector sum          = new PVector(0, 0);
        int     count        = 0;
        for (Boid other : flock.boids) {
            float d = PVector.dist(location, other.location);
            if ((d > 0) && (d < neighbordist)) {
                sum.add(other.velocity);
                count++;
            }
        }
        if (count > 0) {
            sum.div((float) count);
            // First two lines of code below could be condensed with new PVector setMag() method
            // Not using this method until Processing.js catches up
            // sum.setMag(maxspeed);

            // Implement Reynolds: Steering = Desired - Velocity
            sum.normalize();
            sum.mult(maxspeed);
            PVector steer = PVector.sub(sum, velocity);
            steer.limit(maxforce);
            return steer;
        } else {
            return new PVector(0, 0);
        }
    }

    // Cohesion
    // For the average location (i.e. center) of all nearby boids, calculate steering vector towards that location
    PVector cohesion(Flock flock) {
        float   neighbordist = 50;
        PVector sum          = new PVector(0, 0);   // Start with empty vector to accumulate all locations
        int     count        = 0;
        for (Boid other : flock.boids) {
            float d = PVector.dist(location, other.location);
            if ((d > 0) && (d < neighbordist)) {
                sum.add(other.location); // Add location
                count++;
            }
        }
        if (count > 0) {
            sum.div(count);
            return seek(sum);  // Steer towards the location
        } else {
            return new PVector(0, 0);
        }
    }

}
