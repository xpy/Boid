package Boids;

import Boids.Boid.Boid;
import Boids.Flock.Flock;
import processing.core.PApplet;

import java.awt.event.KeyEvent;

/**
 * Boids
 * Created by xpy on 17-Oct-15.
 */
public class Boids extends PApplet {

    Flock flock;

    public void setup() {
        size(800, 600);
        flock = new Flock(this);
        // Add an initial set of boids into the system
        for (int i = 0; i < 150; i++) {
            flock.addBoid();
        }
//        frameRate(1);
    }


    public void draw() {

//        background(50);
        fill(0, 0, 0, 10);
        rect(0, 0, width, height);
        flock.run();
        noFill();
        stroke(255, 100);
        ellipse(mouseX, mouseY, 200, 200);
    }

    public void mousePressed() {
        flock.addBoid(new Boid(this, mouseX, mouseY));
//        flock.separation++;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'q':
                flock.speedBurst+=2;
                break;
            case 'a':
                flock.speedBurst-=2;
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

        }
    }
}
