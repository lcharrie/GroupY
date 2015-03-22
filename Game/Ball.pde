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
    gravity.set(sin(rz) * CONST_G, 0, -sin(rx) * CONST_G);

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
    fill(255, 0, 0);
    translate(0, -(radius + board.sizeY/2), 0);
    translate(location.x, location.y, location.z);
    sphere(radius);
    popMatrix();
  }
  void checkEdges() {
    float maxPosX = board.sizeX/2 - radius;
    float maxPosZ = board.sizeZ/2 - radius;
    if (location.x > maxPosX) { 
      velocity.x = -velocity.x;
      location.x = maxPosX;
    }    
    if (location.x < -maxPosX) { 
      velocity.x = -velocity.x;
      location.x = -maxPosX;
    }
    if (location.z > maxPosZ) {
      velocity.z = -velocity.z;
      location.z = maxPosZ;
    }
    if (location.z < -maxPosZ) {
      velocity.z = -velocity.z;
      location.z = -maxPosZ;
    }
  }

  void checkCylinderCollision() {
    float cylRadius = cylindersCollection.radius;
    for (PVector c : cylindersCollection.list) {
      if (dist(c.x, c.z, this.location.x, this.location.z) < this.radius + cylRadius) {
        PVector n = this.location.get();
        n.sub(c);
        n.normalize(); // n vecteur normal
        PVector newLoc= n.get();
        newLoc.mult(cylRadius+this.radius);
        newLoc.add(c); // new ball location (ext of the cylinder)
        this.location = newLoc;
        PVector nCopy = n.get();
        nCopy.mult(2*velocity.dot(n));
        this.velocity.sub(nCopy);
      }
    }
  }
}

