import processing.core.PApplet;
import pthreading.PThreadManager;

/**
 * PThreading Gravicubes Example
 * 
 * @author micycle1
 *
 */
public final class Example extends PApplet {

	PThreadManager manager;
	Gravicubes x, y;

	boolean frozen = false;

	public static void main(String[] args) {
		PApplet.main(Example.class);
	}

	@Override
	public void settings() {
		size(800, 800);
	}

	public void setup() {

		manager = new PThreadManager(this, 60);

		manager.addThread(Gravicubes.class, 2, 60, 5000); // create two threads using Gravicubes class

		x = new Gravicubes(this, 5000); // create a third thread (instance)
		y = new Gravicubes(this, 5000); // create a fourth thread (instance)
		manager.addThread(x); // add thread instance to manager to run it
		manager.addThread(y); // add thread instance to manager to run it
	}

	public void draw() {
		fill(255, 100);
		rect(0, 0, width, height);
		manager.draw();
	}

	@Override
	public void mousePressed() {
		frozen = !frozen;
		if (frozen) {
			manager.pauseThread(y);
			manager.resumeThread(x);
		} else {
			manager.pauseThread(x);
			manager.resumeThread(y);
		}
	}

	@Override
	public void keyPressed() {
		if (key == 'A') {
			manager.unlinkComputeDraw();
		}
		if (key == 'S') {
			manager.relinkComputeDraw();
		}
		manager.resumeThreads();
	}
}