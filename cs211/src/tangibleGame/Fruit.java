package tangibleGame;

import processing.core.PVector;

public class Fruit {

	TangibleGame parent;
	final int col;
	final String name;
	int size;
	PVector position;

	int colors[] = new int[3];

	public Fruit(TangibleGame p, PVector pV) {
		parent = p;
		colors[0] = p.color(255, 235, 55); // "Citrus"
		colors[1] = p.color(230, 51, 51); // "Apple"
		colors[2] = p.color(250, 145, 53); // "Orange"
		size = (int) p.random(colors.length) + 2;
		col = colors[size - 2];
		switch (size) {
		case 2:
			name = "Citron";
			break;
		case 3:
			name = "Pomme";
			break;
		case 4:
			name = "Orange";
			break;
		default:
			name = "ERROR";
			break;
		}
		this.position = pV;
	}

	void eaten() {
		size--;
		if (size == 0) {
			parent.cylindersCollection.remove(position.x, position.z);
		}
	}
}