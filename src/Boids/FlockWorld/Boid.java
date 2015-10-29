package Boids.FlockWorld;

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
    float   personalSpeed;

    public float speedBurst      = 0;
    public float sizeBurst       = 0;
    public float separationBurst = 0;
    public float colorBurst      = 0;

    public float speedBurstReduce      = .05f;
    public float sizeBurstReduce       = .05f;
    public float separationBurstReduce = .5f;
    public float colorBurstReduce      = .5f;

    private PVector separationSteer;
    private PVector alignmentSteer;
    private PVector cohesionSteer;
    private PVector avoidSteer    = new PVector();
    private PVector approachSteer = new PVector();

    protected PApplet pa;

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

    public void run(World fw, Flock myFlock) {
        normalize();
        for (Flock flock : fw.flocks) {
            flock(flock);
        }
        update(myFlock);
        borders(myFlock);
        render(myFlock);
    }

    void applyForce(PVector force) {
        // We could add mass here if we want A = F / M
        acceleration.add(force);
    }

    // We accumulate a new acceleration each time based on three rules
    void flock(Flock flock) {
        checkNeighbors(flock);
        checkAvoids(flock);
        checkApproaches(flock);
        PVector sep = separationSteer; //separate(flock);   // Separation
        PVector ali = alignmentSteer; //align(flock);      // Alignment
        PVector coh = cohesionSteer;//cohesion(flock);   // Cohesion
//        PVector app       = approach(new PVector(pa.mouseX, pa.mouseY), flock);
//        PVector avo       = avoid(new PVector(pa.mouseX, pa.mouseY), flock);
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
//        dir.add(wallAvoid);

        dir.limit(flock.maxSteer);
//        pa.println(dir);
//        app.mult(0.02f);
//        avo.mult(3.0f);

//        dir.add(app);
        dir.add(avoidSteer);
        dir.add(approachSteer);
        dir.limit(getMaxSpeed(flock));

        applyForce(dir);

    }

    // Method to update location
    void update(Flock flock) {
        velocity.add(acceleration);
        velocity.limit(getWalkSpeed(flock) + acceleration.mag() + personalSpeed);
/*
        pa.stroke(255, 0, 0);
        pa.line(location.x, location.y, location.x + velocity.x, location.y + velocity.y);
        pa.noStroke();
*/
        location.add(velocity);

        acceleration.mult(0);
    }

    PVector avoid(PVector target) {
        PVector desired = PVector.sub(target, location);  // A vector pointing from the location to the target
        return PVector.sub(velocity, desired).normalize(null);
    }

    public void render(Flock flock) {
        pa.fill(getColor(flock));
        pa.stroke(getColor(flock), 100);
        pa.noStroke();
        pa.ellipse(location.x, location.y, getSize(flock), getSize(flock));
      /*
        float theta = velocity.heading() + PApplet.radians(90);
        pa.pushMatrix();
        pa.translate(location.x, location.y);
        pa.rotate(theta);
        pa.beginShape(PApplet.TRIANGLES);
        pa.vertex(0, flock.getSize() * -2);
        pa.vertex(flock.getSize() * -1, flock.getSize() * 2);
        pa.vertex(flock.getSize(), flock.getSize() * 2);
        pa.endShape();
        pa.popMatrix();
      */
    }

    // Wraparound
    void borders(Flock flock) {
        if (location.x < -1 * getSize(flock)) location.x = pa.width + location.x + getSize(flock);
        if (location.y < -1 * getSize(flock)) location.y = pa.height + location.y + getSize(flock);
        if (location.x > pa.width + getSize(flock)) location.x = -getSize(flock) + location.x - pa.width;
        if (location.y > pa.height + getSize(flock)) location.y = -getSize(flock) + location.y - pa.height;
    }

    private boolean canSeeTarget(PVector target, Flock flock) {

        PVector targetVector = PVector.sub(target, location);
        return Math.abs(PApplet.degrees((PVector.angleBetween(this.velocity, targetVector)))) < flock.viewAngle;

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

    public void checkNeighbors(Flock flock) {
        separationSteer = new PVector(0, 0, 0);
        alignmentSteer = new PVector(0, 0, 0);
        cohesionSteer = new PVector(0, 0, 0);
        float manhatanDist     = 0;
        int   cohesionCount    = 0;
        int   separationChecks = 0;
        for (Boid other : flock.getBoids()) {
            manhatanDist = Flock.manhatanDist(location, other.location);
            if (manhatanDist < 1.5 * flock.neighborDist) {
                float d = PVector.dist(location, other.location);
                float d2 = PVector.dist(new PVector(location.x + ((location.x > pa.width / 2) ? -1 : 1) * pa.width, location.y + ((location.y > pa.height / 2) ? -1 : 1) * pa.height), other.location);
                float separation = getSeparation(flock);
                // Check Separation
                if ((d > 0) && manhatanDist < separation && (d < separation || d2 < separation)) {
                    if (true || canSeeTarget(other.location, flock)) {
                        separationChecks++;
                        if (flock.absoluteSeparation) {
                            separationSteer.add(PVector.sub(location, other.location));
                        } else {
                            PVector diff = PVector.sub(other.location, location);
                            PVector normalDiff = diff.normalize(null);
                            normalDiff.mult(getSeparation(flock) * -1);
                            diff = PVector.add(diff, normalDiff);
                            separationSteer.add(diff);
                        }
                    } else if (d == 0 && this != other) {
                        separationSteer.add(PVector.mult(velocity.normalize(null), getSeparation(flock), null));
                    }
                }

                if ((d >= 0) && manhatanDist < flock.neighborDist && (d < flock.neighborDist || (d2 < flock.neighborDist)) && this != other) {

                    // Check alignment
                    if (true || canSeeTarget(other.location, flock)) {
                        alignmentSteer.add(PVector.mult(other.velocity, 1 - d / flock.neighborDist, null));
                        // Check cohesion
                        cohesionSteer.add(other.location); // Add location
                        cohesionCount++;
                    }
                }
            }
        }
        if (cohesionCount > 0) {
            cohesionSteer.div(cohesionCount);
        }
        if (!cohesionSteer.equals(this.location)) {
            cohesionSteer.sub(location);
        }
    }

    public void checkAvoids(Flock flock) {
        avoidSteer = new PVector(0, 0);
        for (int i = 0; i < flock.avoids.size(); i++) {
            Avoid avoid = flock.avoids.get(i);
            float dist = this.location.dist(avoid.location);
            if (dist < avoid.range && dist > 0) {
                PVector newPoint = PVector.sub(location, avoid.location);
                avoidSteer.add(PVector.mult(newPoint, (1 - dist / (flock.neighborDist * 2)) * avoid.power));
            }
        }
    }

    public void checkApproaches(Flock flock) {
        approachSteer = new PVector(0, 0);
        for (int i = 0; i < flock.approaches.size(); i++) {
            Approach approach = flock.approaches.get(i);
            float dist = this.location.dist(approach.location);
            if (dist < approach.range && dist > 0) {
                PVector newPoint = PVector.sub(approach.location, location);
                approachSteer.add(PVector.mult(newPoint, (1 - dist / (flock.neighborDist * 2)) * approach.power));
            }
        }
    }

    // Separation
    // Method checks for nearby boids and steers away
    PVector separate(Flock flock) {
        PVector steer = new PVector(0, 0, 0);
        for (Boid other : flock.getBoids()) {
            float d = PVector.dist(location, other.location);

            // Check Separation
            if ((d > 0) && (d < getSeparation(flock))) {
                if (true || canSeeTarget(other.location, flock)) {
                    if (flock.absoluteSeparation) {
                        steer.add(PVector.sub(location, other.location));
                    } else {
                        PVector diff = PVector.sub(other.location, location);
                        PVector normalDiff = diff.normalize(null);
                        normalDiff.mult(getSeparation(flock) * -1);
                        diff = PVector.add(diff, normalDiff);
                        steer.add(diff);
                    }
                } else if (d == 0 && this != other) {
                    steer.add(PVector.mult(velocity.normalize(null), getSeparation(flock), null));
                }
            }
        }

        return steer;
    }

    // Alignment
    // For every nearby boid in the system, calculate the average velocity
    PVector align(Flock flock) {
        PVector sum = new PVector(0, 0);
        for (Boid other : flock.getBoids()) {

            float d = PVector.dist(location, other.location);
            if ((d >= 0) && (d < flock.neighborDist) && this != other) {
                if (true || canSeeTarget(other.location, flock)) {
                    sum.add(PVector.mult(other.velocity, 1 - d / flock.neighborDist, null));
//                    sum.add(other.velocity);
                }
            }
        }
        return sum;
    }

    // Cohesion
    // For the average location (i.e. center) of all nearby boids, calculate steering vector towards that location
    PVector cohesion(Flock flock) {
        PVector center = getMateCenter(flock);
        if (!center.equals(this.location)) {
            return PVector.sub(center, location);
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
        float dist = location.dist(target);
        if (dist < flock.neighborDist && dist > 0) {
            PVector steer = PVector.sub(target, location);  // A vector pointing from the location to the target
            return PVector.mult(steer, (1 - dist / flock.neighborDist));
        } else {
            return new PVector(0, 0);
        }
    }

    public PVector avoid(PVector point, Flock flock) {
        float dist = this.location.dist(point);
        if (dist < flock.neighborDist * 2 && dist > 0) {
            PVector newPoint = avoid(point);
            return PVector.mult(newPoint, (1 - dist / (flock.neighborDist * 2)));
        } else {
            return new PVector(0, 0);
        }
    }


    public PVector getMateCenter(Flock flock) {
        PVector sum   = new PVector(0, 0);   // Start with empty vector to accumulate all locations
        int     count = 0;
        for (Boid other : flock.getBoids()) {

            float d = PVector.dist(location, other.location);
            if ((d > 0) && (d < flock.neighborDist) && this != other) {
                if (true || canSeeTarget(other.location, flock)) {
                    sum.add(other.location); // Add location
                    count++;
                }
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

    public float getWalkSpeed(Flock flock) {
        return flock.getWalkSpeed() + speedBurst;
    }

    public float getMaxSpeed(Flock flock) {
        return flock.getMaxSpeed() + speedBurst;
    }

    public float getSize(Flock flock) {
        return flock.getSize() + sizeBurst;
    }

    public float getSeparation(Flock flock) {
        return flock.getSeparation() + separationBurst + getSize(flock);
    }

    public int getColor(Flock flock) {
        return pa.color(
                flock.red + (colorBurst + flock.colorBurst) * flock.colorBurstStep,
                flock.green + (colorBurst + flock.colorBurst) * flock.colorBurstStep,
                flock.blue + (colorBurst + flock.colorBurst) * flock.colorBurstStep
        );
    }

}
