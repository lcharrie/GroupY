package tangibleGame;

import processing.core.PShape;
import processing.core.PVector;

public class Montgolfiere {
	TangibleGame parent;
	float flyingHeight;
	final PVector position;
	final int size;
	PShape baloon = new PShape();

	public Montgolfiere(TangibleGame p, PVector position, int size) {
		parent = p;
		this.position = position;
		flyingHeight = position.y;
		this.size = size;
		baloon = p.loadShape("nacelle2.obj");
		p.pushMatrix();
		baloon.scale(4000);
		p.popMatrix();
	}

	void display() {
		if (parent.fly) {
			if (flyingHeight == 0)
				flyingHeight = 1;
			flyingHeight *= 1.1;
		}
		parent.pushMatrix();
		parent.translate(position.x, -flyingHeight * 2 - parent.board.thickness
				/ 2, position.z);
		parent.rotate((float) Math.PI);

		parent.shape(baloon);

		parent.popMatrix();
	}

	PVector getPosition() {
		return position;
	}
}
