package Boids.Boid;

import Boids.Flock.Flock;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Boids
 * Created by xpy on 17-Oct-15.
 */
public class Boid {


    public PVector location;
    PVector velocity;
    PVector acceleration;
    float   r;
    float   wallDistance;
    float   maxforce;    // Maximum steering force
    float   maxSpeed;    // Maximum speed
    float   initialSpeed;    // Maximum speed

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
        maxSpeed = 10;
        initialSpeed = maxSpeed;
        maxforce = 0.2f;
        wallDistance = 20;
    }

    public void setMaxSpeed(float speed) {
        maxSpeed = speed;
    }

    public void setMaxForce(float force) {
        maxforce = force;
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
        PVector sep       = separate(flock);   // Separation
        PVector ali       = align(flock);      // Alignment
        PVector coh       = cohesion(flock);   // Cohesion
        PVector app       = approach(new PVector(pa.mouseX, pa.mouseY), flock);
        PVector wallAvoid = wallAvoid();
        // Arbitrarily weight these forces
        sep.mult(1.0f);
        ali.mult(1.0f);
        coh.mult(1.0f);
        app.mult(5.0f);
        pa.stroke(255, 0, 0);
        pa.line(location.x, location.y, location.x + app.x, location.y + app.y);
        pa.noStroke();
        wallAvoid.mult(100.0f);
        // Add the force vectors to acceleration
        PVector dir = new PVector(0, 0);
        dir.add(sep);
        dir.add(ali);
        dir.add(coh);
        dir.add(app);
        dir.add(wallAvoid);
//        dir.normalize();
        dir.limit(maxforce);
        applyForce(dir);
/*        applyForce(sep);
        applyForce(ali);
        applyForce(coh);
        applyForce(app);
        applyForce(wallAvoid);*/
    }

    // Method to update location
    void update() {
        // Update velocity
        velocity.add(acceleration);
        // Limit speed
        velocity.limit(initialSpeed);
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
        desired.mult(initialSpeed);

        // Above two lines of code below could be condensed with new PVector setMag() method
        // Not using this method until Processing.js catches up
        // desired.setMag(initialSpeed);

        // Steering = Desired minus Velocity
        PVector steer = PVector.sub(desired, velocity).normalize(null);
//        steer.limit(maxforce);  // Limit to maximum steering force
        return steer;
    }

    // A method that calculates and applies a steering force towards a target
    // STEER = DESIRED MINUS VELOCITY
    PVector avoid(PVector target) {
        PVector desired = PVector.sub(target, location);  // A vector pointing from the location to the target
        // Scale to maximum speed
        desired.normalize();
        desired.mult(initialSpeed);

        // Above two lines of code below could be condensed with new PVector setMag() method
        // Not using this method until Processing.js catches up
        // desired.setMag(initialSpeed);

        // Steering = Desired minus Velocity
        PVector steer = PVector.sub(velocity, desired).normalize(null);
//        steer.limit(maxforce);  // Limit to maximum steering force
        return steer;
    }

    public void render() {
        // Draw a triangle rotated in the direction of velocity
        float theta = velocity.heading() + PApplet.radians(90);
        // heading2D() above is now heading() but leaving old syntax until Processing.js catches up

        pa.fill(255);
        pa.stroke(255, 100);
        pa.noStroke();
        pa.ellipse(location.x, location.y, 3, 3);
/*
        pa.pushMatrix();
        pa.translate(location.x, location.y);
        pa.rotate(theta);
        pa.beginShape(PApplet.TRIANGLES);
        pa.vertex(0, -r * 2);
        pa.vertex(-r, r * 2);
        pa.vertex(r, r * 2);
        pa.endShape();
        pa.popMatrix();
*/
    }

    // Wraparound
    void borders() {
        if (location.x < -r) location.x = pa.width + r;
        if (location.y < -r) location.y = pa.height + r;
        if (location.x > pa.width + r) location.x = -r;
        if (location.y > pa.height + r) location.y = -r;
    }

    PVector wallAvoid() {
        PVector futureLocation = PVector.add(location, PVector.mult(velocity, 10));
        PVector avoidVector    = new PVector(0, 0);

        PVector wallVector = new PVector(pa.width, futureLocation.y);
        if (futureLocation.x > pa.width - wallDistance && futureLocation.dist(wallVector) < wallDistance) {
            avoidVector.add(avoid(new PVector(pa.width, location.y)));

        }
        wallVector = new PVector(futureLocation.x, pa.height);
        if (futureLocation.y > pa.height - wallDistance && futureLocation.dist(wallVector) < wallDistance) {
            avoidVector.add(avoid(new PVector(location.x, pa.height)));


        }
        wallVector = new PVector(0, futureLocation.y);
        if (futureLocation.x < wallDistance && futureLocation.dist(wallVector) < wallDistance) {
            avoidVector.add(avoid(new PVector(0, location.y)));


        }
        wallVector = new PVector(futureLocation.x, 0);
        if (futureLocation.y < wallDistance && futureLocation.dist(wallVector) < wallDistance) {
            avoidVector.add(avoid(new PVector(location.x, 0)));


        }


        return avoidVector.normalize(null);


    }

    // Separation
    // Method checks for nearby boids and steers away
    PVector separate(Flock flock) {
        PVector steer = new PVector(0, 0, 0);
        int     count = 0;
        // For every boid in the system, check if it's too close
        for (Boid other : flock.boids) {
            float d = PVector.dist(location, other.location);
            // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
            if ((d > 0) && (d < flock.separation)) {
                // Calculate vector pointing away from neighbor
                PVector diff = PVector.sub(location, other.location);
                diff.normalize();
                diff.mult(1 - d / flock.separation);        // Weight by distance
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
            // steer.setMag(initialSpeed);

            // Implement Reynolds: Steering = Desired - Velocity
            steer.normalize();
            steer.mult(initialSpeed);
            steer.sub(velocity);
//            steer.limit(maxforce);
        }
        return steer.normalize(null);
    }

    // Alignment
    // For every nearby boid in the system, calculate the average velocity
    PVector align(Flock flock) {
        PVector sum   = new PVector(0, 0);
        int     count = 0;
        for (Boid other : flock.boids) {
            float d = PVector.dist(location, other.location);
            if ((d > 0) && (d < flock.neighborDist)) {
                PVector vel = other.velocity;
                vel.normalize();
                sum.add(PVector.mult(vel, 1 - d / flock.neighborDist));
                count++;
            }
        }
        if (count > 0) {
//            sum.div((float) count);
            // First two lines of code below could be condensed with new PVector setMag() method
            // Not using this method until Processing.js catches up
            // sum.setMag(initialSpeed);

            // Implement Reynolds: Steering = Desired - Velocity
            sum.normalize();
            sum.mult(initialSpeed);
//            PVector steer = PVector.sub(sum, velocity);
            sum.sub(velocity);
//            sum.limit(maxforce);
            return sum.normalize(null);
        } else {
            return new PVector(0, 0);
        }
    }

    // Cohesion
    // For the average location (i.e. center) of all nearby boids, calculate steering vector towards that location
    PVector cohesion(Flock flock) {
        PVector center = getMateCenter(flock);
        if (!center.equals(this.location)) {
            PVector newPoint = seek(center);
            float dist = PVector.dist(this.location, center);
            return PVector.mult(newPoint, ( dist / flock.neighborDist) );
        } else {
            return new PVector(0, 0);
        }
    }


    public PVector approach(PVector point, Flock flock) {
        float dist = this.location.dist(point);
        if (dist < flock.neighborDist && dist > 0) {
            PVector newPoint = seek(point);
//            return newPoint;
            return PVector.mult(newPoint, (1 - dist / flock.neighborDist) -.4f);
        } else {
            return new PVector(0, 0);

        }
    }


    public PVector getMateCenter(Flock flock) {
        PVector sum   = new PVector(0, 0);   // Start with empty vector to accumulate all locations
        int     count = 0;
        for (Boid other : flock.boids) {
            float d = PVector.dist(location, other.location);
            if ((d > 0) && (d < flock.neighborDist)) {
                sum.add(other.location); // Add location
                count++;
            }
        }

        if (count > 0) {
            sum.div(count);
            return sum;
        } else {
            return this.location;
        }
    }


    public float calcMaxSpeed(Flock flock) {
        float distFromCenter = PVector.dist(location, flock.getCenter());

        return 10 - (distFromCenter / 10);
    }

}
