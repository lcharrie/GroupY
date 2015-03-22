float frictionMagnitude = normalForce * mu;

class Ball {
  PVector location;
  PVector velocity;
  PVector gravity;
  PVector friction;
  final float radius;

  Ball(float r) {
    location = new PVector(0, 0, 0);
    velocity = new PVector(0, 0, 0);
    gravity = new PVector(0, 0, 0);
    friction = new PVector(0, 0, 0);
    radius = r;
  }
  void update() {
    // compute gravity force
    gravity.set(sin(rz) * CONST_G, 0, -sin(rx) * CONST_G);

    // compute friction force
    friction = velocity.get();
    friction.mult(-1);
    friction.normalize();
    friction.mult(frictionMagnitude);

    velocity.add(gravity);
    velocity.add(friction);

    location.add(velocity);
  }
  void display() {
    pushMatrix();
    fill(ballColor);
    translate(0, -(radius + board.thickness/2), 0); // take the ball out off the ground
    translate(location.x, location.y, location.z);
    sphere(radius);
    popMatrix();
  }
  // ball hit an edge
  void checkEdges() {
    float maxPos = board.size/2 - radius;
    if (location.x > maxPos) { 
      velocity.x = -velocity.x;
      location.x = maxPos;
    }    
    if (location.x < -maxPos) { 
      velocity.x = -velocity.x;
      location.x = -maxPos;
    }
    if (location.z > maxPos) {
      velocity.z = -velocity.z;
      location.z = maxPos;
    }
    if (location.z < -maxPos) {
      velocity.z = -velocity.z;
      location.z = -maxPos;
    }
  }

  /* Ball hit a cylinder:
  ** If a ball is in a cylinder, his position is set to the nearest free location out of the cylinder
  */
  void checkCylinderCollision() {
    float cylRadius = cylindersCollection.radius;
    for (PVector c : cylindersCollection.list) {
      if (dist(c.x, c.z, this.location.x, this.location.z) < this.radius + cylRadius) { // if a ball is in a cylinder
        PVector n = this.location.get();
        n.sub(c);
        n.normalize(); // n vecteur normal
        PVector newLoc= n.get();
        newLoc.mult(cylRadius+this.radius);
        newLoc.add(c); // new ball location (ext of the cylinder)
        this.location = newLoc;
        PVector nCopy = n.get();
        nCopy.mult(2*velocity.dot(n));
        this.velocity.sub(nCopy); // change the direction of the ball
      }
    }
  }
}

