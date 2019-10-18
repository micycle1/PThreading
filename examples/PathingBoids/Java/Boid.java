package micycle.pathing;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Boid {

	private static final int radius = 70;

	private Path path;
	private int currentNode;
	PVector position;
	private PVector velocity, desired;
	private final int pathDir;
	private final PApplet p;
	private final PGraphics g;

	private float max_force, max_velocity;

	public Boid(PApplet p, PGraphics g, PVector position, Path path) {
		this.p = p;
		this.g = g;
		currentNode = (int) p.random(0, path.getNodes().size());
		max_force = p.random(0.001f, 0.2f);
		max_velocity = p.random(0.5f, 5);

		this.position = position;
		velocity = new PVector(0, 0);
		this.path = path;
		pathDir = p.random(0, 2) > 1 ? 1 : -1;
	}

	public void calc() {
		PVector steering = pathFollowing();
		steering = truncate(steering, max_force);
		velocity = PVector.add(velocity, steering);
		velocity = truncate(velocity, max_velocity);
		position = PVector.add(position, velocity);
		position.x = PApplet.constrain(position.x, 0, p.width - 1);
		position.y = PApplet.constrain(position.y, 0, p.height - 1);
	}

	private PVector pathFollowing() {
		PVector target = path.getNode(currentNode);
		if (fastDistance(position, target) <= radius) {
			currentNode += pathDir;
			if (currentNode >= path.size()) {
				currentNode = 0;
			}
			if (currentNode < 0) {
				currentNode = path.size() - 1;
			}
		}
		return target != null ? seek(target) : new PVector(0, 0);
	}

	private PVector seek(PVector target) {
		desired = PVector.sub(target, position);
		desired = desired.normalize();
		desired = PVector.mult(desired, max_velocity);
		return PVector.sub(desired, velocity);
	}

	private static PVector truncate(PVector vector, float max_force) {
		PVector truncated = new PVector(PApplet.constrain(vector.x, -max_force, max_force),
				PApplet.constrain(vector.y, -max_force, max_force));
		return truncated;
	}

	public void drawForces() {
		PVector newVelocity = velocity.copy();
		newVelocity.normalize();
		PVector newDesired = desired.copy();
		newDesired.normalize();
		newVelocity.mult(33);
		g.line(position.x, position.y, position.x + newVelocity.x * 1, position.y + newVelocity.y * 1);
		g.stroke(255, 100, 100);
		g.line(position.x, position.y, position.x + newDesired.x * 10 * max_velocity,
				position.y + newDesired.y * 10 * max_velocity);
	}

	private static double fastDistance(PVector a, PVector b) {
		double d = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
		return Double.longBitsToDouble(((Double.doubleToLongBits(d) - (1l << 52)) >> 1) + (1l << 61));
	}
}