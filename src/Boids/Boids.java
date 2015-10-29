package Boids;

import Boids.FlockWorld.Avoid;
import Boids.FlockWorld.Flock;
import Boids.FlockWorld.World;
import processing.core.PApplet;

import java.awt.event.KeyEvent;

/**
 * Boids
 * Created by xpy on 17-Oct-15.
 */
public class Boids extends PApplet {

    Flock flock;
    World fw = new World();

    public void setup() {
        size(800, 600, P3D);
        // Add an initial set of boids into the system
//        for (int i = 0; i < 1; i++) {
        flock = new Flock(this);
        for (int j = 0; j < 500; j++) {
            flock.addBoid();
        }
            fw.addFlock(flock);
//        }
//        frameRate(1);
    }


    public void draw() {

        background(0);
//        fill(0, 0, 0, 10);
//        rect(0, 0, width, height);
        fw.update();
        noFill();
        stroke(255, 100);
        ellipse(mouseX, mouseY, 200, 200);
    }

    public void mousePressed() {
//        flock.addBoid(new Boid(this, mouseX, mouseY));
        flock.addAvoid(new Avoid(this, 1, 80, mouseX, mouseY));
//        flock.separation++;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'q':
                flock.speedBurst += 5;
                break;
            case 'a':
                flock.speedBurst -= 2;
                break;
            case 'w':
                flock.separationBurst += 10;
                break;
            case 's':
                flock.separationBurst -= 10;
                break;
            case 'e':
                flock.sizeBurst += 1;
                break;
            case 'd':
                flock.sizeBurst -= 1;
                break;
            case 'r':
                flock.colorBurst += 20;
                break;
            case 'f':
                flock.colorBurst -= 20;
                break;
            case 't':
                flock.separationFactor += .1f;
                break;
            case 'g':
                flock.separationFactor -= .1f;
                break;
            case 'y':
                flock.alignmentFactor += .1f;
                break;
            case 'h':
                flock.alignmentFactor -= .1f;
                break;
            case 'u':
                flock.cohesionFactor += .1f;
                break;
            case 'j':
                flock.cohesionFactor -= .1f;
                break;

        }
    }
}
