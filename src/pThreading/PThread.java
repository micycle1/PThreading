package pThreading;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * PThread. Extend this class, overriding the {@link #calc()} and
 * {@link #draw()} methods with your own code.
 * 
 * @author micycle1
 *
 */
public abstract class PThread implements Runnable {

	protected PGraphics pg;
	protected PApplet p;

	public PThread(PApplet p) {
		this.p = p;
		pg = p.createGraphics(p.width, p.height);
	}

	/**
	 * Don't override this
	 * @hidden
	 */
	@Override
	public final void run() {
		pg.beginDraw();
		pg.clear();
		calc();
		draw();
		pg.endDraw();
	}

	protected final void render() {
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

}
