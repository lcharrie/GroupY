package tangibleGame;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

class Cylinders {
	TangibleGame parent;
	ArrayList<PVector> list;
	ArrayList<Fruit> fruits;
	// PShape cylinder = new PShape();
	final float radius;

	Cylinders(TangibleGame p, float radius) {
		parent = p;
		this.radius = radius;
		list = new ArrayList<PVector>();
		fruits = new ArrayList<Fruit>();
	}

	// display the whool set of cylinders
	void display() {
		for (int i = 0; i < list.size(); i++) {
			PVector c = list.get(i);
			Fruit f = fruits.get(i);
			parent.pushMatrix();
			parent.translate(0, -parent.board.thickness / 2 - f.size * radius
					/ 2, 0); // set
			// the
			// cylinders
			// out
			// of
			// the
			// board
			parent.translate(c.x, 0, c.z);
			parent.rotateX((float) (Math.PI / 2));

			parent.fill(fruits.get(i).col);
			parent.sphere(fruits.get(i).size * radius / 2);
			parent.popMatrix();
		}
	}

	ArrayList<PVector> getCylinders() {
		return list;
	}

	/*
	 * Try to add a new cylender to the set* You can't build two cylinders in
	 * the same spot
	 */
	void add(float x, float y) {
		PVector p = new PVector(x, 0, y);
		Fruit f = new Fruit(parent, p);
		if (Math.max(Math.abs(x), Math.abs(y)) <= (parent.board.size / 2)
				- radius * f.size / 2) { // check if the location is in the
											// board area
			for (PVector c : list) {
				if (PApplet.dist(x, y, c.x, c.z) < f.size * radius) { // ckeck
																		// if
																		// there
																		// isnt
																		// an
																		// other
																		// cylinder
																		// in
																		// the
																		// area
					return;
				}
			}
			for (int i = 0; i < parent.ball.chenille.length; i++) {
				if (PApplet.dist(x, y, parent.ball.chenille[i].x,
						parent.ball.chenille[i].z) < f.size * radius / 2
						+ parent.ball.radius) {
					return;
				}
			}
			if (PApplet.dist(x, y, parent.montgolfiere.position.x,
					parent.montgolfiere.position.z) < f.size * radius / 2
					+ parent.montgolfiere.size) {
				return;
			}

			list.add(p);
			fruits.add(f);
		}
	}

	void remove(float x, float z) {
		for (int i = 0; i < list.size(); i++) {
			PVector c = list.get(i);
			if ((x - c.x) * (x - c.x) + (z - c.z) * (z - c.z) <= radius
					* radius) {
				list.remove(i);
				fruits.remove(i);
			}
		}
	}

	public void addWhile(int threshold) {
		while (calculatePoints() < threshold) {
			add((int) parent.random(-430, 430), (int) parent.random(-430, 430));
		}
		parent.available = calculatePoints();
	}

	private int calculatePoints() {
		int points = 0;
		for (int i = 0; i < list.size(); i++) {
			switch (fruits.get(i).size) {
			case 1:
				points += 10;
				break;
			case 2:
				points += 15;
				break;
			case 3:
				points += 17;
				break;
			case 4:
				points += 18;
				break;
			default:
				break;
			}
		}
		return points;
	}
}
