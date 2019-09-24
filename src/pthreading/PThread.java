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

	private boolean timing = true;

	/**
	 * Will be at most target fps, useful is lower and you wanbt to see performace
	 * or thwether your thread is more cpu than draw bound. (in millis)
	 */
	protected long calcTime, drawTime;

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
	 * Merely instantiating a thread will not run it. Add it to a
	 * {@link pthreading.PThreadManager PThreadManager}.
	 * 
	 * @param p
	 */
	public PThread(PApplet p) {
		this.p = p;
		pg = p.createGraphics(p.width, p.height);
		r = new Runnable() {
			public void run() {
				pg.beginDraw();
				pg.clear();
				if (timing) {
					final long t1 = System.nanoTime();
					calc();
					final long t2 = System.nanoTime();
					draw();
					final long t3 = System.nanoTime();
					calcTime = (t2 - t1);
					drawTime = (t3 - t2);
				} else {
					calc();
					draw();
				}
				pg.endDraw();
			}
		};
	}

	/**
	 * Renders the thread's PGraphics into the parent PApplet.
	 */
	final void render() {
		p.image(pg, 0, 0);
		timing = false;
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
	
	public int getCalcTime() {
		timing = true;
		return (int) (calcTime/1000000);
	}
}
