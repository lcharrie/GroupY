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
    translate(0, -(radius + board.sizeY/2), 0);
    translate(location.x, location.y, location.z);
    sphere(radius);
    popMatrix();
  }
  void checkEdges() {
    if (abs(location.x) > board.sizeX/2 - radius) { 
      velocity.set(-velocity.x, velocity.y, velocity.z);
    }
    if (abs(location.z) > board.sizeZ/2 - radius) {
      velocity.set(velocity.x, velocity.y, -velocity.z);
    }
  }

  void checkCylinderCollision() {
    for (PVector c : cylindersCollection.list) {
      if (dist(c.x, c.z, this.location.x, this.location.z) <= this.radius + cylindersCollection.radius){
        PVector n = this.location.get();
        n.sub(c);
        n.normalize();
        
        n.mult(2*velocity.dot(n));
        velocity.sub(n);
      }
    }
  }
}

