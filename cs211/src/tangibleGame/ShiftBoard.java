package tangibleGame;

import processing.core.PVector;

public class ShiftBoard {

	TangibleGame parent;
	final float ratio;

	ShiftBoard(TangibleGame p, float ratio) {
		parent = p;
		this.ratio = ratio;
	}

	void display() {
		// diplay the board
		parent.fill(parent.grassColor);
		parent.rect(-parent.board.size / 4, -parent.board.size / 4,
				parent.board.size / 2, parent.board.size / 2);
		// display the ball
		parent.fill(parent.chenilleColor);
		if (!parent.fly) {
			for (int i = 0; i < parent.ball.chenille.length; i = i + 2) {
				parent.ellipse(parent.ball.chenille[i].x * ratio,
						parent.ball.chenille[i].z * ratio, parent.ball.radius
								* 2 * ratio, parent.ball.radius * 2 * ratio);
			}
		}
		// display the cylinders
		for (int i = 0; i < parent.cylindersCollection.list.size(); ++i) {
			PVector c = parent.cylindersCollection.list.get(i);
			parent.fill(parent.cylindersCollection.fruits.get(i).col);
			parent.ellipse(c.x * ratio, c.z * ratio,
					parent.cylindersCollection.fruits.get(i).size
							* parent.cylindersCollection.radius * ratio,
					parent.cylindersCollection.fruits.get(i).size
							* parent.cylindersCollection.radius * ratio);
		}
		PVector p = parent.montgolfiere.getPosition();
		parent.fill(0, 0, 255);
		parent.ellipse(p.x * ratio, p.z * ratio, parent.montgolfiere.size * 4
				* ratio, parent.montgolfiere.size * 4 * ratio);
	}
}
