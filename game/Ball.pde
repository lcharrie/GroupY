float frictionMagnitude = normalForce * mu;

class Ball {
  PVector velocity;
  PVector gravity;
  PVector friction;
  final float radius;
  ArrayList<PVector> trace;
  float lastVelocity, currentVelocity = 0;
  PVector chenille[];
  
  Ball(float r) {
    velocity = new PVector(0, 0, 0);
    gravity = new PVector(0, 0, 0);
    friction = new PVector(0, 0, 0);
    trace = new ArrayList<PVector>();
    chenille = new PVector[10];
    for(int i = 0; i < chenille.length; i++) {
      chenille[i] = new PVector(0, 0, 0);
    }
    radius = r;
    
  }
  void update() {
    // compute gravity force
    gravity.set(sin(rz) * CONST_G, 0, -sin(rx) * CONST_G);
    
    for(int i = chenille.length - 1; i > 0; i--) {
      chenille[i] = chenille[i - 1];
    }
    // compute friction force
    friction = velocity.get();
    friction.mult(-1);
    friction.normalize();
    friction.mult(frictionMagnitude);

    velocity.add(gravity);
    velocity.add(friction);

    chenille[0] = chenille[0].get();
    chenille[0].add(velocity);
    
    currentVelocity = computeVelocity();
    
    PVector p = new PVector(chenille[0].x, chenille[0].y, chenille[0].z);
    boolean already = false;
    for(PVector t : trace) {
      if(t.x == p.x && t.z == p.z) {
        already = true;
      }
    }
    if(!already) {
      if(trace.size() > 200) {
        trace.remove(0);
      }
      trace.add(p);
    }
  }
  void display() {
    pushMatrix();
    fill(chenilleColor);
    translate(0, -(radius + board.thickness/2), 0); // take the ball out off the ground
    for(int i = 0; i < ball.chenille.length; i = i + 2) {
      translate(chenille[i].x, chenille[i].y, chenille[i].z);
      sphere(radius);
      translate(-chenille[i].x, -chenille[i].y, -chenille[i].z);
    }
    popMatrix();
  }
  // ball hit an edge
  void checkEdges() {
    float maxPos = board.size/2 - radius;
    if (chenille[0].x > maxPos) { 
      velocity.x = -velocity.x;
      chenille[0].x = maxPos;
      lastVelocity = -currentVelocity;
      score += lastVelocity;
      if(score != 0) {
        previousScores.append((int) score);
      }
    }    
    if (chenille[0].x < -maxPos) { 
      velocity.x = -velocity.x;
      chenille[0].x = -maxPos;
      lastVelocity = -currentVelocity;
      score += lastVelocity;   
    }
    if (chenille[0].z > maxPos) {
      velocity.z = -velocity.z;
      chenille[0].z = maxPos;
      lastVelocity = -currentVelocity;
      score += lastVelocity;
    }
    if (chenille[0].z < -maxPos) {
      velocity.z = -velocity.z;
      chenille[0].z = -maxPos;
      lastVelocity = -currentVelocity;
      score += lastVelocity;
    }
    score = max(score, 0);
  }

  /* Ball hit a cylinder:
  ** If a ball is in a cylinder, his position is set to the nearest free location out of the cylinder
  */
  void checkCylinderCollision() {
    float cylRadius = cylindersCollection.radius;
    for (PVector c : cylindersCollection.list) {
      if (dist(c.x, c.z, this.chenille[0].x, this.chenille[0].z) < this.radius + cylRadius) { // if a ball is in a cylinder
        PVector n = this.chenille[0].get();
        n.sub(c);
        n.normalize(); // n vecteur normal
        PVector newLoc= n.get();
        newLoc.mult(cylRadius+this.radius);
        newLoc.add(c); // new ball location (ext of the cylinder)
        this.chenille[0] = newLoc;
        PVector nCopy = n.get();
        nCopy.mult(2*velocity.dot(n));
        this.velocity.sub(nCopy); // change the direction of the ball
        lastVelocity = computeVelocity(); 
        score += lastVelocity;
      }
    }
  }
  
  ArrayList<PVector> getTrace() {
    return trace;
  }
  
  float computeVelocity() {
    return sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z);
  }
}

