package pthreading;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * PThread. Extend this class, overriding the {@link #calc()} and
 * {@link #draw()} methods with your own code. Prefix every call to a Processing
 * method with
 * 
 * @author micycle1
 *
 */
public abstract class PThread {

	/**
	 * Draw into this.
	 */
	protected PGraphics pg;

	/**
	 * Exposed in {@link #PThread(PApplet) PThread} so that you can refer to of the
	 * parent PApplet (like mouseX, or ) in your code.
	 */
	protected PApplet p;
	Runnable r;

	/**
	 * Merely instantiating a thread will not run it. Add it to a {@link pthreading.PThreadManager PThreadManager}. 
	 * @param p
	 */
	public PThread(PApplet p) {
		this.p = p;
		pg = p.createGraphics(p.width, p.height);
		r = new Runnable() {
			public void run() {
				pg.beginDraw();
				pg.clear();
				calc();
				draw();
				pg.endDraw();
			}
		};
	}

	/**
	 * Renders the thread's PGraphics into the parent PApplet.
	 */
	final void render() {
		p.image(pg, 0, 0);
	}

	/**
	 * Must be overrided. Put code that
	 */
	public abstract void draw();

	/**
	 * Optional override (you can do calculation-related code in {@link #draw()}.
	 * This is useful when the 'unthread drawing' flag is true
	 */
	public void calc() {
	}

	void clearPGraphics() {
		pg.beginDraw();
		pg.clear();
		pg.endDraw();
	}
}