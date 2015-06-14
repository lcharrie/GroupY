class Montgolfiere {
  float flyingHeight;
  final PVector position;
  final int size;
  PShape baloon = new PShape();
  
  public Montgolfiere(PVector position, int size){
    this.position = position;
    flyingHeight = position.y;
    this.size = size;
    baloon = loadShape("nacelle2.obj");
    pushMatrix();
    baloon.scale(4000);
    popMatrix();
  }
  
  void display() {
    if(fly) {
      if (flyingHeight == 0) flyingHeight = 1;
     flyingHeight *= 1.1;
    }
    pushMatrix();
    translate(position.x, -flyingHeight * 2 - board.thickness / 2, position.z);
    rotate(PI);
    shape(baloon);
    //fill(color(0,0,255));
    //sphere(size * 2);
    popMatrix();
  }
  
  PVector getPosition() {
    return position;
  }
}
