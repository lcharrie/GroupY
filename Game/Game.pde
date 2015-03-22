// Physics
final float CONST_G = 0.5;
final float normalForce = 1;
final float mu = 0.1;

// Set colors
final color grassColor = color(0, 102, 0);
final color ballColor = color(255, 0, 0);
final color treeColor = color(204, 102, 0);

Ball ball;
Board board;
Cylinders cylindersCollection;
ShiftBoard shiftBoard;

float camSpeed = 1.0;
float rx, rz, ry = 0.0;
boolean shiftMode = false;

void setup() {
  size(1000, 1000, P3D);
  noStroke();

  ball = new Ball(30);
  board = new Board(1000, 25);
  cylindersCollection = new Cylinders(40);
  shiftBoard = new ShiftBoard((width/2) / board.size);
}

void draw() {
  background(220);
  if (shiftMode) {
    camera();
    // shiftMode on : display 2D board
    pushMatrix();
    translate(width/2, height/2);
    shiftBoard.display();
    popMatrix();
  } else {
    camera(width/2, height/2 - 500, 1000, width/2, width/2, 0, 0, 1, 0);
    directionalLight(120, 100, 80, 1, 1, 0);
    ambientLight(90, 90, 90);
    // shiftMode off : display and run the game
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
    // press 'a' to show axis
    if (keyPressed && key == 97) shapeAxis(1000);
    popMatrix();
  }
}

void mouseClicked() {
  if (shiftMode) {
    // try to add a cylinder to the set (shiftMode on)
    cylindersCollection.add(mouseX*2 - width, mouseY*2 - height);
  }
}

void mouseDragged() {
  if (!shiftMode) {
    // swift the plate (max 60% and shiftMode off)
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
    case SHIFT:
      shiftMode = true; // turn shiftMode on
    default:
      break;
    }
  }
}

void keyReleased() {
  shiftMode = false; // turn shiftMode off
}

void mouseWheel(MouseEvent event) {
  // speed of the swift
  camSpeed = max(1.0, camSpeed - event.getCount());
}

// shape the axis
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

