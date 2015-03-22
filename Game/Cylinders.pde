class Cylinders {
  ArrayList<PVector> list = new ArrayList<PVector>();
  PShape cylinder = new PShape();
  final float radius = 40;

  Cylinders() {
    cylinder = createShape();
    shapeCylinder(radius, 60, 40);
  }

  void display() {
    for (PVector c : list) {
      pushMatrix();
      translate(c.x, - board.sizeY / 2, c.z);
      rotateX(PI / 2 );
      shape(cylinder);
      popMatrix();
    }
  }

  void add(float x, float y) {
    if (max(abs(x), abs(y)) <= (board.sizeX / 2) - radius) {
      for (PVector c : list) {
        if (dist(x, y, c.x, c.z) < 2 * radius) {
          return;
        }
      }
      list.add(new PVector(x, 0, y));
    }
  }

  void shapeCylinder(float cylinderBaseSize, float cylinderHeight, int cylinderResolution) {
    float angle;
    float[] x = new float[cylinderResolution + 1];
    float[] y = new float[cylinderResolution + 1];
    
    //get the x and y position on a circle for all the sides
    for (int i = 0; i < x.length; i++) {
      angle = (TWO_PI / cylinderResolution) * i;
      x[i] = sin(angle) * cylinderBaseSize;
      y[i] = cos(angle) * cylinderBaseSize;
    }
    
    cylinder.beginShape(TRIANGLE_STRIP);
    //draw bottom shape
    for (int i = 0; i < x.length; i++) {
      cylinder.vertex( x[i], y[i], 0);
      cylinder.vertex( 0, 0, 0);
    }

    //draw top shape
    for (int i = 0; i < x.length; i++) {
      cylinder.vertex( x[i], y[i], cylinderHeight);
      cylinder.vertex( 0, 0, cylinderHeight);
    }

    //draw the border of the cylinder
    for (int i = 0; i < x.length; i++) {
      cylinder.vertex(x[i], y[i], 0);
      cylinder.vertex(x[i], y[i], cylinderHeight);
    }
    cylinder.endShape(CLOSE);
  }
}

