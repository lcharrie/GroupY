// Physics
final float CONST_G = 0.5;
final float normalForce = 1;
final float mu = 0.1;

Ball ball;
Board board;
Cylinders cylindersCollection;

float camSpeed = 1.0;
float rx, rz, ry = 0.0;

void setup() {
  size(1000, 1000, P3D);
  noStroke();
  ball = new Ball(30);
  board = new Board(1000, 25, 1000);
  cylindersCollection = new Cylinders();
}
void draw() {
  background(220);
  if (keyPressed && keyCode == SHIFT) {
    camera();
    displayShiftMode();
  } else {
    camera(width/2, height/2 - 500, 2000, board.sizeX/2, board.sizeZ/2, 0, 0, 1, 0);
    directionalLight(120, 100, 80, 1, 1, 0);
    ambientLight(90, 90, 90);

    pushMatrix();  
    translate(width/2, height/2, 0);
    rotateX(rx);
    rotateY(ry);
    rotateZ(rz);
    board.display();
    ball.update();
    ball.checkEdges();
    ball.checkCylinderCollision();
    ball.display();
    cylindersCollection.display();
    if (keyPressed && key == 97) shapeAxis(1000);
    popMatrix();
  }
}

void mouseClicked() {
  if (keyPressed && keyCode == SHIFT) {
    cylindersCollection.add(mouseX*2 - width, mouseY*2 - height);
  }
}

void mouseDragged() {
  if (!(keyPressed && keyCode == SHIFT)) {
    rx = max(-TWO_PI/6, min(TWO_PI/6, rx - (mouseY - pmouseY) / 180.0 * camSpeed));
    rz = max(-TWO_PI/6, min(TWO_PI/6, rz + (mouseX - pmouseX) / 180.0 * camSpeed));
  }
}

void keyPressed() {
  if (key == CODED) {
    switch(keyCode) {
    case LEFT:
      ry += (1 / 90.0)*camSpeed;
      break;
    case RIGHT:
      ry -= (1 / 90.0)*camSpeed;
      break;
    default:
      break;
    }
  }
}

void mouseWheel(MouseEvent event) {
  camSpeed = max(1.0, camSpeed - event.getCount());
}

void displayShiftMode() {
  pushMatrix();
  translate(width/2, height/2);
  fill(150);
  rect(-width/4, -height/4, width/2, height/2);
  fill(50);
  ellipse(ball.location.x / 2, ball.location.z / 2, ball.radius, ball.radius);
  for (PVector c : cylindersCollection.list) {
    ellipse(c.x / 2, c.z / 2, cylindersCollection.radius, cylindersCollection.radius);
  }
  fill(255);
  popMatrix();
}

void shapeAxis(float size) {
  strokeWeight(2);
  stroke(0, 255, 0);
  line(-size, 0, 0, size, 0, 0); //x = redMover
  stroke(0, 0, 255);
  line(0, -size, 0, 0, size, 0); //y = green
  stroke(255, 0, 0);
  line(0, 0, -size, 0, 0, size); //z = blue
  noStroke();
}

