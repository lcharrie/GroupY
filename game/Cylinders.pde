//Cylinders are actually fruits

class Cylinders {
  ArrayList<PVector> list;
  ArrayList<Fruit> fruits;
  //PShape cylinder = new PShape();
  final float radius;

  Cylinders(float radius) {
    this.radius = radius;
    list = new ArrayList<PVector>();
    fruits = new ArrayList<Fruit>();
  }

  // display the whool set of cylinders
  void display() {
    for (int i = 0; i < list.size(); i++) {
      PVector c = list.get(i);
      Fruit f = fruits.get(i);
      pushMatrix();
      translate(0, - board.thickness / 2 - f.size * radius / 2, 0); // set the cylinders out of the board
      translate(c.x, 0, c.z);
      rotateX(PI / 2 );
      
      fill((color) fruits.get(i).col);
      sphere(fruits.get(i).size * radius / 2);
      popMatrix();
    }
  }
  
  ArrayList<PVector> getCylinders() {
    return list;
  }

  /* Try to add a new cylender to the set
  ** You can't build two cylinders in the same spot
  */
  void add(float x, float y) {
    PVector p = new PVector(x, 0, y); 
    Fruit f = new Fruit(p);
    if (max(abs(x), abs(y)) <= (board.size / 2) - radius * f.size / 2) { // check if the location is in the board area
      for (PVector c : list) {
        if (dist(x, y, c.x, c.z) < f.size * radius) { // ckeck if there isnt an other cylinder in the area
          return;
        }
      }
      for(int i = 0; i < ball.chenille.length; i++) {
        if(dist(x, y, ball.chenille[i].x, ball.chenille[i].z) < f.size * radius / 2 + ball.radius) {
          return;
        }
      }
      if(dist(x, y, montgolfiere.position.x, montgolfiere.position.z) < f.size * radius / 2 + montgolfiere.size) {
        return;
      }
      
      list.add(p);
      fruits.add(f);
    }
  }

  void remove(float x, float z) {
    for(int i = 0; i < list.size(); i++) {
      PVector c = list.get(i);
      if((x - c.x) * (x - c.x) + (z - c.z) * (z - c.z) <= radius * radius) {
        list.remove(i);
        fruits.remove(i);
      } 
    }
  }
  
  public void addWhile(int threshold) {
    while(calculatePoints() < threshold) {
      add((int) random(-430, 430), (int) random(-430, 430));
    }
    available = calculatePoints();
  }
  
  private int calculatePoints() {
    int points = 0;
    for(int i = 0; i < list.size(); i++) {
      switch(fruits.get(i).size){
        case 1 : points += 10; break;
        case 2 : points += 15; break;
        case 3 : points += 17; break;
        case 4 : points += 18; break;
        default : break;
      }
    }
    return points;
  }
}



/*
  // Shape a cylinder
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
  */
