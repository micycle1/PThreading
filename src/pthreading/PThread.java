package pthreading;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Extend this class, overriding the {@link #calc()} and {@link #draw()} methods
 * with your own code, then use a {@link pthreading.PThreadManager
 * PThreadManager} to run the thread.
 * 
 * <p>
 * Prefix every call to a Processing draw method with g -- for example:
 * <i>g.rect(10,10,10,10);</i>
 * </p>
 * <p>
 * Refer to any PApplet variable by prefixing it with p -- for example:
 * <i>p.mousePressed</i>.
 * </p>
 * 
 * @author micycle1
 *
 */
public abstract class PThread {

	private boolean timing = false;

	long calcTime, drawTime;

	/**
	 * The PGraphics object the thread draws into.
	 */
	protected PGraphics g;

	/**
	 * Exposed so that any subclasses can access PApplet variables (such as mouse
	 * coords).
	 */
	protected final PApplet p;

	final Runnable r, noCalc;

	/**
	 * Constructs a thread.
	 * <p>
	 * NOTE: Merely instantiating a thread will not run it. Add it to a
	 * {@link pthreading.PThreadManager PThreadManager} for it to execute.
	 * 
	 * @param p Parent PApplet
	 */
	public PThread(PApplet p) {
		this.p = p;
		g = p.createGraphics(p.width, p.height);
		r = new Runnable() {
			public void run() {
				g.beginDraw();
				g.clear();
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
				g.endDraw();
			}
		};
		noCalc = new Runnable() {
			public void run() {
				g.beginDraw();
				g.clear();
				if (timing) {
					final long t2 = System.nanoTime();
					draw();
					final long t3 = System.nanoTime();
					drawTime = (t3 - t2);
				} else {
					draw();
				}
				g.endDraw();
			}
		};
	}

	/**
	 * The heart of a PThread. Override this method with <b> code that should be
	 * executed in a thread.</b> Prefix calls to processing draw functions with
	 * <i>g.</i> (eg. <i>g.ellipse(50, 50, 50, 50)</i>.
	 * <p>
	 * Internally, this method is called after {@link #calc()}.
	 * 
	 * @see #calc()
	 */
	protected abstract void draw();

	/**
	 * An optional override (you <i>can</i> do calculation-related code in
	 * {@link #draw()}. <b>This code will be executed in a thread.</b> This is
	 * useful when the 'unthread drawing' flag is true.
	 * <p>
	 * Internally, this method is called before {@link #draw()}.
	 * 
	 * @see #draw()
	 */
	protected void calc() {
	}

	void clearPGraphics() {
		g.beginDraw();
		g.clear();
		g.endDraw();
	}

	/**
	 * Returns time taken for the thread's draw() loop to execute. Can be used with
	 * {@link #getCalcFPS()} to determine if a thread is calculation bound or
	 * draw-call bound.
	 * 
	 * @return draw() execution time (milliseconds)
	 */
	final public float getDrawFPS() {
		timing = true;
		return 1000/(drawTime / 1000000f);
	}

	/**
	 * Returns time taken for the thread's calc() loop to execute. Can be used with
	 * {@link #getDrawFPS()} to determine if a thread is calculation bound or
	 * draw-call bound.
	 * 
	 * @return calc() execution time (milliseconds)
	 */
	final public float getCalcFPS() {
		timing = true;
		return 1000/(calcTime / 1000000f);
	}

	/**
	 * Enables the collection of timing information (draw and calc time). May incur
	 * a slight overhead. By default, timing information is not enabled.
	 * 
	 * @see #disableTiming()
	 * @see #getDrawFPS()
	 * @see #getCalcFPS()
	 */
	final public void enableTiming() {
		timing = true;
	}

	/**
	 * Disables the collection of timing information (draw and calc time). By
	 * default, timing information is not enabled.
	 * 
	 * @see #enableTiming()
	 */
	final public void disableTiming() {
		timing = false;
	}
}
