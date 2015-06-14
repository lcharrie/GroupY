package tangibleGame;

import java.util.Comparator;

import processing.core.PVector;

public class VectorComparator implements Comparator<PVector> {
	int arg;

	public VectorComparator(int arg) {
		this.arg = arg;
	}

	@Override
	public int compare(PVector l1, PVector l2) {
		if (arg == 0){
			if (l1.x < l2.x)
				return -1;
			return 1;
		} else {
			if (l1.y < l2.y)
				return -1;
			return 1;
		}
	}
}