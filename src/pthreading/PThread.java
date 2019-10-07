package pthreading;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * PThread. Extend this class, overriding the {@link #calc()} and
 * {@link #draw()} methods with your own code.
 * 
 * <p>
 * Prefix every call to a Processing draw method with g(raphics) -- for example:
 * <i>g.rect(10,10,10,10);</i>
 * <p>
 * 
 * <p>
 * Refer to any PApplet variable by prefixing it with p -- for example:
 * <i>p.mousePressed</i>
 * <p/>
 * 
 * @author micycle1
 *
 */
public abstract class PThread {

	private boolean timing = false;

	protected long calcTime, drawTime;

	/**
	 * The PGraphics object the thread should draw into.
	 */
	protected PGraphics g;

	/**
	 * Exposed so that any subclasses can access PApplet variables (such as mouse
	 * coords).
	 */
	protected final PApplet p;

	/**
	 * Exposed in {@link #PThread(PApplet) PThread} so that you can refer to of the
	 * parent PApplet (like mouseX, or ) in your code.
	 */
	Runnable r;

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
	}

	/**
	 * This method must be overridden.
	 * 
	 * Override this method with code that
	 * 
	 * <b>This code will be executed in a thread.</b> Put code that. Remember to
	 * prefix calls to processing draw functions with <i>pthread.___()</i> (eg.
	 * <i>pthread.ellipse(50, 50, 50, 50)</i> Internally, this method is called
	 * after {@link #calc()}.
	 * 
	 * @see #calc()
	 */
	protected abstract void draw();

	/**
	 * An optional override (you <i>can</i> do calculation-related code in
	 * {@link #draw()}. <b>This code will be executed in a thread.</b> This is
	 * useful when the 'unthread drawing' flag is true. Internally, this method is
	 * called before {@link #draw()}.
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
	 * {@link #getCalcTime()} to determine if a thread is calculation bound or
	 * draw-call bound.
	 * 
	 * @return draw() execution time (milliseconds)
	 */
	final public float getDrawTime() {
		timing = true;
		return (drawTime / 1000000f);
	}

	/**
	 * Returns time taken for the thread's calc() loop to execute. Can be used with
	 * {@link #getDrawTime()} to determine if a thread is calculation bound or
	 * draw-call bound.
	 * 
	 * @return calc() execution time (milliseconds)
	 */
	final public float getCalcTime() {
		timing = true;
		return (calcTime / 1000000f);
	}

	/**
	 * Enables the collection of timing information (draw and calc time). May incur
	 * a slight overhead. By default, timing information is not enabled.
	 * 
	 * @see #disableTiming()
	 * @see #getDrawTime()
	 * @see #getCalcTime()
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
