package tangibleGame;

import java.util.ArrayList;
import processing.core.*;

public class Ball {
	TangibleGame parent;

	PVector velocity;
	PVector gravity;
	PVector friction;
	final float radius;
	ArrayList<PVector> trace;
	float currentVelocity = 0;
	PVector chenille[];
	float frictionMagnitude;

	Ball(TangibleGame p, float r) {
		parent = p;
		velocity = new PVector(0, 0, 0);
		gravity = new PVector(0, 0, 0);
		friction = new PVector(0, 0, 0);
		trace = new ArrayList<PVector>();
		chenille = new PVector[10];
		for (int i = 0; i < chenille.length; i++) {
			chenille[i] = new PVector(0, 0, 0);
		}
		radius = r;
		frictionMagnitude = p.normalForce * p.mu;

	}

	void update() {
		// compute gravity force
		gravity.set((float) Math.sin(parent.rz) * parent.CONST_G, 0.f,
				(float) -Math.sin(parent.rx) * parent.CONST_G);

		for (int i = chenille.length - 1; i > 0; i--) {
			chenille[i] = chenille[i - 1];
		}
		// compute friction force
		friction = velocity.get();
		friction.mult(-1);
		friction.normalize();
		friction.mult(frictionMagnitude);

		velocity.add(gravity);
		velocity.add(friction);

		chenille[0] = chenille[0].get();
		chenille[0].add(velocity);

		currentVelocity = computeVelocity();

		PVector p = new PVector(chenille[0].x, chenille[0].y, chenille[0].z);
		boolean already = false;
		for (PVector t : trace) {
			if (t.x == p.x && t.z == p.z) {
				already = true;
			}
		}
		if (!already) {
			if (trace.size() > 200) {
				trace.remove(0);
			}
			trace.add(p);
		}
	}

	void display() {
		parent.pushMatrix();
		parent.fill(parent.chenilleColor);
		parent.translate(0, -(radius + parent.board.thickness / 2), 0); // take
																		// the
																		// ball
																		// out
																		// off
																		// the
																		// ground
		for (int i = 0; i < parent.ball.chenille.length; i = i + 2) {
			parent.translate(chenille[i].x, chenille[i].y, chenille[i].z);
			parent.sphere(radius);
			if (i == 0) {
				float dx = chenille[0].x - chenille[1].x;
				float dz = chenille[0].z - chenille[1].z;

				if (dx == 0.0 && dz == 0.0) {
					dz = 30.f;
				} else {
					float norme = (float) Math.sqrt(dx * dx + dz * dz);
					dx = dx / norme * radius;
					dz = dz / norme * radius;
				}

				// anthènes
				parent.stroke(1);
				parent.strokeWeight(4);
				parent.line(0, 0, 0, -dz, -2 * radius, dx);
				parent.line(0, 0, 0, dz, -2 * radius, -dx);
				parent.noStroke();
				parent.translate(-dz, -2 * radius, dx);
				parent.fill(parent.ballTraceColor);
				parent.sphere(radius / 2);
				parent.translate(2 * dz, 0, -2 * dx);
				parent.sphere(radius / 2);
				parent.translate(-dz, 2 * radius, dx);

				// yeux
				parent.translate(dx, 0, dz);
				parent.sphere(radius / 8);
				parent.translate(-dz / 2, -radius / 2, dx / 2);
				parent.sphere(radius / 4);
				parent.translate(dz, 0, -dx);
				parent.sphere(radius / 4);
				parent.translate(-dx - dz / 2, radius / 2, -dz + dx / 2);

				parent.fill(parent.chenilleColor);
			}
			parent.translate(-chenille[i].x, -chenille[i].y, -chenille[i].z);
		}
		parent.popMatrix();
	}

	// ball hit an edge
	void checkEdges() {
		float maxPos = parent.board.size / 2 - radius;
		if (chenille[0].x > maxPos) {
			velocity.x = -velocity.x;
			chenille[0].x = maxPos;

		}
		if (chenille[0].x < -maxPos) {
			velocity.x = -velocity.x;
			chenille[0].x = -maxPos;

		}
		if (chenille[0].z > maxPos) {
			velocity.z = -velocity.z;
			chenille[0].z = maxPos;
		}
		if (chenille[0].z < -maxPos) {
			velocity.z = -velocity.z;
			chenille[0].z = -maxPos;
		}
	}

	/*
	 * Ball hit a cylinder:* If a ball is in a cylinder, his position is set to
	 * the nearest free location out of the cylinder
	 */
	void checkCylinderCollision() {
		float cylRadius = parent.cylindersCollection.radius;
		for (int i = 0; i < parent.cylindersCollection.list.size(); ++i) {
			PVector c = parent.cylindersCollection.list.get(i);
			Fruit f = parent.cylindersCollection.fruits.get(i);
			if (PApplet.dist(c.x, c.z, this.chenille[0].x, this.chenille[0].z) < this.radius
					+ f.size * cylRadius / 2) { // if a ball is in a cylinder
				int diff = 0;
				switch (f.size) {
				case 4:
					diff = 1;
					break;
				case 3:
					diff = 2;
					break;
				case 2:
					diff = 5;
					break;
				case 1:
					diff = 10;
					break;
				default:
					break;
				}
				parent.score += diff;
				parent.available -= diff;
				parent.lastEaten = f.name;
				f.eaten();
				PVector n = this.chenille[0].get();
				n.sub(c);
				n.normalize(); // n vecteur normal
				PVector newLoc = n.get();
				newLoc.mult(cylRadius + this.radius);
				newLoc.add(c); // new ball location (ext of the cylinder)
				this.chenille[0] = newLoc;
				PVector nCopy = n.get();
				nCopy.mult(2 * velocity.dot(n));
				this.velocity.sub(nCopy); // change the direction of the ball
			}
		}
	}

	void checkMontgolfiereCollision() {
		PVector p = parent.montgolfiere.getPosition();
		if (PApplet.dist(p.x, p.z, this.chenille[0].x, this.chenille[0].z) < this.radius
				+ parent.montgolfiereSize * 2) { // if a ball is in the montgolfier
			if (parent.score < parent.minimalScore) {
				PVector n = this.chenille[0].get();
				n.sub(p);
				n.normalize(); // n vecteur normal
				PVector newLoc = n.get();
				newLoc.mult(parent.montgolfiereSize * 2 + this.radius);
				newLoc.add(p); // new ball location (ext of the cylinder)
				this.chenille[0] = newLoc;
				PVector nCopy = n.get();
				nCopy.mult(2 * velocity.dot(n));
				this.velocity.sub(nCopy); // change the direction of the ball
			} else {
				parent.fly = true;
				parent.gameTime = (parent.millis() - parent.shiftModeTime - parent.startTime) / 1000;
			}
		}
	}

	ArrayList<PVector> getTrace() {
		return trace;
	}

	float computeVelocity() {
		return (float) Math.sqrt(velocity.x * velocity.x + velocity.y
				* velocity.y + velocity.z * velocity.z);
	}
}
