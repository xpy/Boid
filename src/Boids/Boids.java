package Boids;

import Boids.Boid.Boid;
import Boids.Flock.Flock;
import Boids.FlockWorld.FlockWorld;
import processing.core.PApplet;

import java.awt.event.KeyEvent;

/**
 * Boids
 * Created by xpy on 17-Oct-15.
 */
public class Boids extends PApplet {

    Flock flock;
    FlockWorld fw = new FlockWorld();

    public void setup() {
        size(800, 600);
        // Add an initial set of boids into the system
        for (int i = 0; i < 5; i++) {
            flock = new Flock(this);
            for (int j = 0; j < 50; j++) {
                flock.addBoid();
            }
            fw.addFlock(flock);
        }
//        frameRate(1);
    }


    public void draw() {

//        background(50);
        fill(0, 0, 0, 10);
        rect(0, 0, width, height);
        fw.update();
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
                flock.boids.get(0).speedBurst+=5;
                break;
            case 'a':
                flock.boids.get(0).speedBurst-=2;
                break;
            case 'w':
                flock.boids.get(0).separationBurst += 10;
                break;
            case 's':
                flock.boids.get(0).separationBurst -= 10;
                break;
            case 'e':
                flock.boids.get(0).sizeBurst += 1;
                break;
            case 'd':
                flock.boids.get(0).sizeBurst -= 1;
                break;
            case 'r':
                flock.boids.get(0).colorBurst += 20;
                break;
            case 'f':
                flock.boids.get(0).colorBurst -= 20;
                break;

        }
    }
}
