package tangibleGame;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.IntList;
import processing.event.MouseEvent;
import processing.video.Movie;

@SuppressWarnings("serial")
public class TangibleGame extends PApplet {
	// Physics
	final float CONST_G = 0.5f;
	final float normalForce = 1;
	final float mu = 0.02f;
	final int cylinderSize = 20;
	final int montgolfiereSize = 30;

	// Set colors
	final int grassColor = color(0, 102, 0);
	// final color ballColor = color(255, 0, 0);
	final int chenilleColor = color(255, 255, 0);
	final int treeColor = color(204, 102, 0);
	final int ballTraceColor = color(150, 0, 0);
	final int visualisationColor = color(255, 250, 205);
	final int chartColor = color(240, 235, 190);
	final int scoreColor = color(0, 0, 205);

	// Surfaces
	PGraphics visualisationArea;
	PGraphics topView;
	PGraphics scoreboard;
	static PGraphics barChart;

	// Objects
	Ball ball;
	Board board;
	Cylinders cylindersCollection;
	Montgolfiere montgolfiere;
	Start start;
	ShiftBoard shiftBoard;
	HScrollbar scoreScrollbar;
	ComputeAngles computer;

	Movie cam;
	PImage img;

	float camSpeed = 1.0f;
	float rx, rz, ry = 0.0f;
	boolean startMode = true;
	boolean shiftMode = false;
	boolean shiftModeEnable = false;
	float depth = 2000;
	int score = 0;
	int minimalScore = 0;
	int topViewShift = 15;
	int topViewSize;
	IntList previousScores;
	int counter = 0;
	int factor = 1;
	boolean fly = false;
	int shiftModeTime = 0;
	int shiftModeTimeStart;
	int startTime = 0;
	int gameTime;
	int available = 0;
	String lastEaten = "";

	public void setup() {
		size(1000, 1000, P3D);
		noStroke();

		rx = 0;
		ry = 0;
		rz = 0;
		score = 0;
		counter = 0;
		factor = 1;
		fly = false;
		startTime = 0;
		gameTime = 0;
		shiftModeTime = 0;
		lastEaten = "";
		previousScores = new IntList();
		topViewSize = height / 4 - 2 * topViewShift;

		visualisationArea = createGraphics(width, height / 4, P3D);
		topView = createGraphics(topViewSize, topViewSize, P3D);
		scoreboard = createGraphics(topViewSize, topViewSize, P3D);
		barChart = createGraphics(width - 4 * topViewShift - scoreboard.width
				- topView.width, topViewSize, P3D);

		ball = new Ball(this, 30);
		board = new Board(this, 1000, 25);
		cylindersCollection = new Cylinders(this, cylinderSize);
		montgolfiere = new Montgolfiere(this, new PVector(300, 0, 400),
				montgolfiereSize);
		start = new Start(this);
		shiftBoard = new ShiftBoard(this, (width / 2) / board.size);
		scoreScrollbar = new HScrollbar(this, 0, barChart.height - 30,
				barChart.width, 30);

		cam = new Movie(this, "testvideo.mp4");
		cam.loop();

		computer = new ComputeAngles(this);

	}

	public void draw() {
		background(220);
		if (startMode) {
			camera();
			// shiftMode on : display 2D board
			pushMatrix();
			translate(width / 2, height / 2);
			start.display();
			popMatrix();
		} else if (shiftModeEnable && shiftMode && !fly) {
			if (shiftModeTimeStart == 0) {
				shiftModeTimeStart = millis();
			}
			camera();
			// shiftMode on : display 2D board
			pushMatrix();
			translate(width / 2, height / 2);
			shiftBoard.display();
			popMatrix();
		} else {
			if (startTime == 0) {
				startTime = millis();
			}
			if (shiftModeTimeStart != 0) {
				shiftModeTime += (millis() - shiftModeTimeStart);
				shiftModeTimeStart = 0;
			}
			directionalLight(120, 120, 120, 1, 1, 0);
			ambientLight(102, 102, 102);
			camera(width / 2, height / 2 - 500, depth, width / 2, width / 2, 0,
					0, 1, 0);

			counter++;
			if (counter >= frameRate && !fly) {
				counter = 0;
				if (score != 0) {
					previousScores.append((int) score);
				}
			}

			scoreScrollbar.update();

			PVector angles = computer.getAngles(img);
			if (angles != null) {
				rx = angles.x;
				ry = angles.z;
				rz = angles.y;
			}

			// shiftMode off : display and run the game
			pushMatrix();
			translate(width / 2, height / 2, 0);
			rotateX(rx);
			rotateY(ry);
			rotateZ(rz);
			board.display();
			cylindersCollection.display();
			montgolfiere.display();

			// press 'a' to show axis
			if (keyPressed && key == 97)
				shapeAxis(1000);

			if (fly) {
				// afficher la tête de la chenille dans la nacelle
				popMatrix();
				textSize(72);
				text("Bravo vous avez gagné !\nVous avez récolté " + score
						+ " portions de nourriture \nen " + gameTime
						+ " secondes.", -400, -400, 0);
				textSize(48);
				text("Presser SUPPRIMER pour retourner au menu principal",
						-300, 0, 0);
			} else {
				ball.update();
				ball.checkEdges();
				ball.checkCylinderCollision();
				ball.checkMontgolfiereCollision();
				ball.display();
				popMatrix();

			}

			drawVisualisationArea();
			drawTopView();
			drawScoreboard();
			drawBarChart();
			camera();
			noLights();
			image(visualisationArea, 0, (float) (3.0 / 4.0 * height));
			image(topView, topViewShift,
					(float) (3.0 / 4.0 * height + topViewShift));
			image(scoreboard, topViewShift * 2 + topViewSize,
					(float) (3.0 / 4.0 * height + topViewShift));
			image(barChart,
					scoreboard.width + topView.width + 3 * topViewShift,
					(float) (3.0 / 4.0 * height + topViewShift));
		}

		if (cam.available() == true) {
			cam.read();
		}
		img = cam.get();

		img.resize(img.width / 3, img.height / 3);
		image(img, 0, 0);
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
		if (previousScores.size() > 0) {
			if (previousScores.get(previousScores.size() - 1) * 1.3 / factor > barChart.height) {
				factor *= 2;
			}
		}
		for (int i = 0; i < previousScores.size(); i++) {
			for (int j = 0; j < previousScores.get(i)
					/ ((columnWidth + 1) * factor); j++) {
				barChart.rect(
						5 + (columnWidth + 1) * i * scoreScrollbar.getPos() * 2,
						barChart.height - (columnWidth + 1) * j
								- scoreScrollbar.barHeight - 20, columnWidth
								* scoreScrollbar.getPos() * 2, columnWidth);
			}
		}
		// scoreScrollbar.display();
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
		scoreboard.text("Portions mangées : ", 10, 25);
		scoreboard.text("Dernier fruit mangé : ", 10, 75);
		scoreboard.text("Portions Disponibles : ", 10, 125);
		scoreboard.text("Portions nécessaires : ", 10, 175);
		scoreboard.textSize(12);
		scoreboard.text(score, 10, 50);
		scoreboard.text(lastEaten, 10, 100);
		scoreboard.text(available, 10, 150);
		scoreboard.text(minimalScore, 10, 200);
		scoreboard.endDraw();
	}

	void drawTopView() {
		topView.beginDraw();
		topView.background(grassColor);
		topView.noStroke();
		float a = topViewSize / board.size;
		for (int i = 0; i < cylindersCollection.list.size(); ++i) {
			PVector c = cylindersCollection.list.get(i);
			topView.fill(cylindersCollection.fruits.get(i).col);
			topView.ellipse((c.x + board.size / 2) * a, (c.z + board.size / 2)
					* a, cylindersCollection.fruits.get(i).size * cylinderSize
					* a, cylindersCollection.fruits.get(i).size * cylinderSize
					* a);
		}

		if (!fly) {
			topView.fill(ballTraceColor);
			ArrayList<PVector> ballTrace = ball.getTrace();
			for (PVector t : ballTrace) {
				topView.ellipse((t.x + board.size / 2) * a,
						(t.z + board.size / 2) * a, ball.radius * a,
						ball.radius * a);
			}

			topView.fill(chenilleColor);
			for (int i = 0; i < ball.chenille.length; i++) {
				topView.ellipse((ball.chenille[i].x + board.size / 2) * a,
						(ball.chenille[i].z + board.size / 2) * a, ball.radius
								* a * 2, ball.radius * a * 2);
			}

			topView.fill(0, 0, 255);
			topView.ellipse((montgolfiere.position.x + board.size / 2) * a,
					(montgolfiere.position.z + board.size / 2) * a,
					montgolfiere.size * a * 4, montgolfiere.size * a * 4);

		}
		topView.endDraw();
	}

	void drawVisualisationArea() {
		visualisationArea.beginDraw();
		visualisationArea.background(visualisationColor);
		visualisationArea.endDraw();
	}

	public void mouseClicked(MouseEvent event) {
		if (shiftMode) {
			if (event.getButton() == LEFT) {
				// try to add a cylinder to the set (shiftMode on)
				cylindersCollection
						.add(mouseX * 2 - width, mouseY * 2 - height);
			} else if (event.getButton() == RIGHT) {
				cylindersCollection.remove(mouseX * 2 - width, mouseY * 2
						- height);
			}
		}
	}

	public void keyPressed() {
		start.action();
		if (key == BACKSPACE) {
			startMode = true;
		}

		if (key == CODED) {
			switch (keyCode) {
			case LEFT:
				ry += (1 / 90.0) * camSpeed;
				break;
			case RIGHT:
				ry -= (1 / 90.0) * camSpeed;
				break;
			case SHIFT:
				shiftMode = true; // turn shiftMode on
				break;
			default:
				break;
			}
		}
	}

	public void keyReleased() {
		shiftMode = false; // turn shiftMode off
	}

	public void mouseWheel(MouseEvent event) {
		// speed of the swift
		camSpeed = (float) Math.min(Math.max(0.1, camSpeed - event.getCount()),
				10);
	}

	// shape the axis
	void shapeAxis(float size) {
		strokeWeight(2);
		stroke(0, 255, 0);
		line(-size, 0, 0, size, 0, 0); // x = redMover
		stroke(0, 0, 255);
		line(0, -size, 0, 0, size, 0); // y = green
		stroke(255, 0, 0);
		line(0, 0, -size, 0, 0, size); // z = blue
		noStroke();
	}

}
