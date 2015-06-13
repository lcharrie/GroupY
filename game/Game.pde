// Physics
final float CONST_G = 0.5;
final float normalForce = 1;
final float mu = 0.02;
final int cylinderSize = 40;

// Set colors
final color grassColor = color(0, 102, 0);
final color ballColor = color(255, 0, 0);
final color chenilleColor = color(255, 255, 0);
final color treeColor = color(204, 102, 0);
final color ballTraceColor = color(150, 0, 0);
final color visualisationColor = color(255, 250, 205);
final color chartColor = color(240, 235, 190);
final color scoreColor = color(0, 0, 205);

// Surfaces
PGraphics visualisationArea;
PGraphics topView;
PGraphics scoreboard;
static PGraphics barChart;

// Objects
Ball ball;
Board board;
Cylinders cylindersCollection;
ShiftBoard shiftBoard;
HScrollbar scoreScrollbar;

float camSpeed = 1.0;
float rx, rz, ry = 0.0;
boolean shiftMode = false;
float depth = 2000;
float score = 0;
int topViewShift = 15;
int topViewSize;
IntList previousScores;
int counter = 0;
int factor = 1;

void setup() {
  size(1000, 1000, P3D);
  noStroke();

  previousScores = new IntList();
  topViewSize = height / 4 - 2 * topViewShift;

  visualisationArea = createGraphics(width, height / 4, P3D);
  topView = createGraphics(topViewSize, topViewSize, P3D);
  scoreboard = createGraphics(topViewSize * 3 / 4, topViewSize, P3D);
  barChart = createGraphics(width - 4 * topViewShift - scoreboard.width - topView.width, topViewSize, P3D);

  ball = new Ball(30);
  board = new Board(1000, 25);
  cylindersCollection = new Cylinders(cylinderSize);
  shiftBoard = new ShiftBoard((width/2) / board.size);
  scoreScrollbar = new HScrollbar(0, barChart.height - 30, barChart.width, 30);
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
    directionalLight(120, 120, 120, 1, 1, 0);
    ambientLight(102, 102, 102);
    camera(width/2, height/2 - 500, depth, width/2, width/2, 0, 0, 1, 0);
    
    counter++;
    if(counter >= frameRate) {
      counter = 0;
      if(score != 0) {
        previousScores.append((int) score);
      } 
    }
    
    scoreScrollbar.update();
    
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
        
    drawVisualisationArea();
    drawTopView();
    drawScoreboard();
    drawBarChart();
    camera();
    noLights();
    image(visualisationArea, 0, 3.0 / 4.0 * height);
    image(topView, topViewShift, 3.0 / 4.0 * height + topViewShift);
    image(scoreboard, topViewShift * 2 + topViewSize, 3.0 / 4.0 * height + topViewShift);
    image(barChart, scoreboard.width + topView.width + 3 * topViewShift, 3.0 / 4.0 * height + topViewShift);
  }
}

void drawBarChart() {
  barChart.beginDraw();
  barChart.background(chartColor);
  barChart.noFill();
  barChart.stroke(grassColor);
  barChart.strokeWeight(5);
  barChart.beginShape();
  barChart.vertex(0, 0);
  barChart.vertex(0, barChart.height - 40);
  barChart.vertex(barChart.width, barChart.height - 40);
  barChart.vertex(barChart.width, 0);
  barChart.endShape(CLOSE);
  
  int columnWidth = 5;
  barChart.fill(scoreColor);
  barChart.noStroke();
  if(previousScores.size() > 0) {
    if(previousScores.get(previousScores.size() - 1) * 1.3 / factor > barChart.height){
      factor *= 2;
    }
  }
  for(int i = 0; i < previousScores.size(); i ++) {
    for(int j = 0; j < previousScores.get(i) / ((columnWidth + 1) * factor); j++) {
      barChart.rect(5 + (columnWidth + 1) * i * scoreScrollbar.getPos() * 2, barChart.height - (columnWidth + 1) * j - scoreScrollbar.barHeight - 20, columnWidth * scoreScrollbar.getPos() * 2, columnWidth);
    }
  }
  scoreScrollbar.display();
  barChart.endDraw();
}

void drawScoreboard() {
  scoreboard.beginDraw();
  scoreboard.background(visualisationColor);
  scoreboard.noFill();
  scoreboard.stroke(grassColor);
  scoreboard.strokeWeight(5);
  scoreboard.beginShape();
  scoreboard.vertex(0, 0);
  scoreboard.vertex(0, scoreboard.height);
  scoreboard.vertex(scoreboard.width, scoreboard.height);
  scoreboard.vertex(scoreboard.width, 0);
  scoreboard.endShape(CLOSE);
  scoreboard.fill(grassColor);
  scoreboard.textSize(16);
  scoreboard.text("Total score : \n" + score, 10, 25);
  scoreboard.text("Velocity : \n" + ball.currentVelocity, 10, 100);
  scoreboard.text("Last score : \n" + ball.lastVelocity, 10, 175);
  scoreboard.endDraw();
}

void drawTopView() {
  topView.beginDraw();
  topView.background(grassColor);
  topView.noStroke();
  topView.fill(treeColor);
  ArrayList<PVector> cylinderList = cylindersCollection.getCylinders();
  float a = topViewSize / board.size; 
  for (PVector c : cylinderList) {
    topView.ellipse((c.x + board.size / 2) * a, (c.z + board.size / 2) * a, cylinderSize * a * 2, cylinderSize * a * 2);
  }

  topView.fill(ballTraceColor);
  ArrayList<PVector> ballTrace = ball.getTrace();
  for (PVector t : ballTrace) {
    topView.ellipse((t.x + board.size / 2) * a, (t.z + board.size / 2) * a, ball.radius * a, ball.radius * a);
  }
  topView.fill(chenilleColor);
  for(int i = 0; i < ball.chenille.length; i++) {
    topView.ellipse((ball.chenille[i].x + board.size / 2) * a, (ball.chenille[i].z + board.size / 2) * a, ball.radius * a * 2, ball.radius * a * 2);
  }
  topView.endDraw();
}

void drawVisualisationArea() {
  visualisationArea.beginDraw();
  visualisationArea.background(visualisationColor);
  visualisationArea.endDraw();
}

void mouseClicked(MouseEvent event) {
  if (shiftMode) {
    if (event.getButton() == LEFT) {
      // try to add a cylinder to the set (shiftMode on)
      cylindersCollection.add(mouseX*2 - width, mouseY*2 - height);
    } else if(event.getButton() == RIGHT){
      cylindersCollection.remove(mouseX * 2 - width, mouseY * 2 - height);
    }
  }
}

void mouseDragged() {
  if (!shiftMode && mouseY < 3 * height / 4) {
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
  camSpeed = min(max(0.1, camSpeed - event.getCount()), 10);
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

