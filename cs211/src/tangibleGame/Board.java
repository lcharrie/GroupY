package tangibleGame;

public class Board {
	TangibleGame parent;
	final float size;
	final float thickness;

	Board(TangibleGame p, float size, float thickness) {
		this.size = size;
		this.thickness = thickness;
		parent = p;
	}

	void display() {
		parent.fill(parent.grassColor);
		parent.box(size, thickness, size);
	}
}
