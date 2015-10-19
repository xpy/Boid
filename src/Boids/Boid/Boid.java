package Boids.Boid;

import Boids.Flock.Flock;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.Random;

// TODO Optimize with one loop... maybe...

/**
 * Boids
 * Created by xpy on 17-Oct-15.
 */
public class Boid {


    public PVector location;
    PVector velocity;
    PVector acceleration;
    float   size;
    float   wallDistance;
    float   walkSpeed;    // Maximum steering force
    float   initialSpeed;    // Maximum steering force
    float   personalSpeed;
    private PApplet pa;

    public Boid(PApplet pa, float x, float y) {
        this.pa = pa;
        acceleration = new PVector(0, 0);

        float angle = pa.random(PApplet.TWO_PI);
        velocity = new PVector(PApplet.cos(angle), PApplet.sin(angle));
        location = new PVector(x, y);
        size = 2.0f;

        walkSpeed = 0.1f;
        velocity.limit(walkSpeed);
        personalSpeed = new Random().nextFloat() * .5f;
        wallDistance = 20;
    }

    public void run(Flock flock) {
        flock(flock);
        update(flock);
        borders(flock);
        render(flock);
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
        PVector avo       = avoid(new PVector(pa.mouseX, pa.mouseY), flock);
        PVector wallAvoid = wallAvoid();
        PVector dir       = new PVector(0, 0);

        // Arbitrarily weight these forces
        sep.mult(flock.separationFactor);
        ali.mult(flock.alignmentFactor);
        coh.mult(flock.cohesionFactor);
        wallAvoid.mult(100.0f);


        dir.add(sep);
        dir.add(ali);
        dir.add(coh);
        dir.add(wallAvoid);


        dir.limit(flock.maxSteer);
//        pa.println(dir);
        app.mult(1.0f);
        avo.mult(3.0f);


//        dir.add(app);
        dir.add(avo);
        dir.limit(flock.getMaxSpeed());

        applyForce(dir);

    }

    // Method to update location
    void update(Flock flock) {
        velocity.add(acceleration);
        velocity.limit(flock.getWalkSpeed() + acceleration.mag() + personalSpeed);

/*
        pa.stroke(255, 0, 0);
        pa.line(location.x, location.y, location.x + velocity.x, location.y + velocity.y);
        pa.noStroke();
*/
//PApplet.println(velocity);
        location.add(velocity);
/*
        if(velocity.mag() > 1){
            velocity.setMag(1);
        }
*/
        acceleration.mult(0);
    }

    // A method that calculates and applies a steering force towards a target
    // STEER = DESIRED MINUS VELOCITY
    PVector seek(PVector target) {
        PVector desired = PVector.sub(target, location);  // A vector pointing from the location to the target
        PVector steer   = PVector.sub(desired, velocity);
        return steer;
    }

    PVector avoid(PVector target) {
        PVector desired = PVector.sub(target, location);  // A vector pointing from the location to the target
        PVector steer   = PVector.sub(velocity, desired).normalize(null);
        return steer;
    }

    public void render(Flock flock) {
        // Draw a triangle rotated in the direction of velocity
        float theta = velocity.heading() + PApplet.radians(90);
        // heading2D() above is now heading() but leaving old syntax until Processing.js catches up

        pa.fill(flock.getColor());
        pa.stroke(flock.getColor(), 100);
        pa.noStroke();
        pa.ellipse(location.x, location.y, flock.getSize(), flock.getSize());
      /*  pa.pushMatrix();
        pa.translate(location.x, location.y);
        pa.rotate(theta);
        pa.beginShape(PApplet.TRIANGLES);
        pa.vertex(0, flock.getSize() * -2);
        pa.vertex(flock.getSize() * -1, flock.getSize() * 2);
        pa.vertex(flock.getSize(), flock.getSize() * 2);
        pa.endShape();
        pa.popMatrix();*/
    }

    // Wraparound
    void borders(Flock flock) {
        if (location.x < -1 * flock.getSize()) location.x = pa.width + flock.getSize();
        if (location.y < -1 * flock.getSize()) location.y = pa.height + flock.getSize();
        if (location.x > pa.width + flock.getSize()) location.x = -flock.getSize();
        if (location.y > pa.height + flock.getSize()) location.y = -flock.getSize();
    }

    private boolean canSeeTarget(PVector target, Flock flock) {

        PVector targetVector = PVector.sub(location, target);
        return PApplet.degrees((PVector.angleBetween(this.velocity, targetVector))) < flock.viewAngle;

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
        for (Boid other : flock.boids) {
            float d = PVector.dist(location, other.location);

            if ((d > 0) && (d < flock.getSeparation())) {

//                PVector diff = PVector.sub(location, other.location);
                PVector diff = PVector.sub(other.location, location);
                PVector normalDiff = diff.normalize(null);
                normalDiff.mult(flock.getSeparation() * -1);
                diff = PVector.add(diff, normalDiff);
//                diff.mult(1 - d / flock.separation);        // Weight by distance
                steer.add(diff);
                count++;            // Keep track of how many
            } else if (d == 0 && this != other) {
                steer.add(PVector.mult(velocity.normalize(null), flock.getSeparation(), null));
            }
        }
        // Average -- divide by how many
/*
        if (count > 0) {
            steer.div((float) count);
        }
*/

        // As long as the vector is greater than 0
/*
        if (steer.mag() > 0) {
            steer.sub(velocity);
        }
*/
        return steer;
    }

    // Alignment
    // For every nearby boid in the system, calculate the average velocity
    PVector align(Flock flock) {
        PVector sum   = new PVector(0, 0);
        int     count = 0;
        for (Boid other : flock.boids) {
            float d = PVector.dist(location, other.location);
            if ((d >= 0) && (d < flock.neighborDist) && this != other) {
                PVector vel = other.velocity;
//                vel.normalize();
                sum.add(PVector.mult(vel, 1 - d / flock.neighborDist, null));
//                sum.add(vel);
                count++;
            }
        }
        if (count > 0) {
//            sum.div(count);
//            sum.sub(velocity);
            return sum;
        } else {
            return new PVector(0, 0);
        }
    }

    // Cohesion
    // For the average location (i.e. center) of all nearby boids, calculate steering vector towards that location
    PVector cohesion(Flock flock) {
        PVector center = getMateCenter(flock);
        if (!center.equals(this.location)) {
//            PVector steer = seek(center);
            PVector steer = PVector.sub(center, location);  // A vector pointing from the location to the target

            float dist = PVector.dist(this.location, center);
//            steer =  PVector.mult(steer, (dist / flock.getSeparation()));
//            steer.normalize();
            steer.sub(velocity);
            return steer;
//            return PVector.mult(steer, (dist / flock.neighborDist));
        } else {
            return new PVector(0, 0);
        }
    }


    public PVector approach(PVector target, Flock flock) {
/*
        // For Circle Around Point
        PVector desired = PVector.sub(target, location);  // A vector pointing from the location to the target
        desired.normalize();
        desired.mult(50);
        target.sub(desired);
*/
        float dist = this.location.dist(target);
        if (dist < flock.neighborDist && dist > 0) {
            PVector steer = seek(target);
//            steer.add(align(flock));
//            return steer;
            return PVector.mult(steer, (1 - dist / flock.neighborDist));
        } else {
            return new PVector(0, 0);

        }
    }

    public PVector avoid(PVector point, Flock flock) {
        float dist = this.location.dist(point);
        if (dist < flock.neighborDist * 2 && dist > 0) {
            PVector newPoint = avoid(point);
//            return newPoint;
            return PVector.mult(newPoint, (1 - dist / (flock.neighborDist * 2)));
        } else {
            return new PVector(0, 0);

        }
    }


    public PVector getMateCenter(Flock flock) {
        PVector sum   = new PVector(0, 0);   // Start with empty vector to accumulate all locations
        int     count = 0;
        for (Boid other : flock.boids) {
            float d = PVector.dist(location, other.location);
            if ((d > 0) && (d < flock.neighborDist) && this != other) {
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
