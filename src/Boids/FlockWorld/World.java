package Boids.FlockWorld;

import java.util.ArrayList;
import java.util.List;

/**
 * World
 * Created by xpy on 21-Oct-15.
 */
public class World {

    public List<Flock> flocks = new ArrayList<>();


    public void addFlock(Flock flock) {
        flocks.add(flock);
    }

    public void update() {
        for (Flock f : flocks) {
            f.run(this);
        }
    }
}
