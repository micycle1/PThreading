package pthreading;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * PThread. Extend this class, overriding the {@link #calc()} and
 * {@link #draw()} methods with your own code.
 * 
 * <p>
 * Prefix every call to a Processing draw method with pthread -- for example:
 * <i>pthread.rect(10,10,10,10);</i>
 * <p>
 * 
 * <p>
 * Refer to any PApplet variable by prefixing it with papplet -- for example:
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
	protected PGraphics pthread;

	/**
	 * Exposed so that any subclasses can access PApplet variables (such as mouse
	 * coords).
	 */
	protected final PApplet papplet;

	/**
	 * Exposed in {@link #PThread(PApplet) PThread} so that you can refer to of the
	 * parent PApplet (like mouseX, or ) in your code.
	 */
	Runnable r;

	/**
	 * Constructs a thread. NOTE: Merely instantiating a thread will not run it. Add
	 * it to a {@link pthreading.PThreadManager PThreadManager} for it to execute.
	 * 
	 * @param p
	 */
	public PThread(PApplet p) {
		this.papplet = p;
		pthread = p.createGraphics(p.width, p.height);
		r = new Runnable() {
			public void run() {
				pthread.beginDraw();
				pthread.clear();
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
				pthread.endDraw();
			}
		};
	}

	/**
	 * Must be overridden. <b>This code will be executed in a thread.</b> Put code
	 * that. Remember to prefix calls to processing draw functions with
	 * pthread.___() Internally, this method is called after {@link #calc()}.
	 * 
	 * @see #calc()
	 */
	protected abstract void draw();

	/**
	 * Optional override (you <i>can</i> do calculation-related code in
	 * {@link #draw()}. <b>This code will be executed in a thread.</b> This is
	 * useful when the 'unthread drawing' flag is true. Internally, this method is
	 * called before {@link #draw()}.
	 * 
	 * @see #draw()
	 */
	protected void calc() {
	}

	void clearPGraphics() {
		pthread.beginDraw();
		pthread.clear();
		pthread.endDraw();
	}

	/**
	 * Returns time taken for the draw loop to execute. Can be used with
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
	 * 
	 * @return calc() execution time (milliseconds)
	 */
	final public float getCalcTime() {
		timing = true;
		return (calcTime / 1000000f);
	}

	/**
	 * Enables the collection of the last timing information. (May incur a slight
	 * overhead)
	 * 
	 * @see #disableTiming()
	 * @see #getDrawTime()
	 * @see #getCalcTime()
	 */
	final public void enableTiming() {
		timing = true;
	}

	/**
	 * Disable timing information.
	 * 
	 * @see #enableTiming()
	 */
	final public void disableTiming() {
		timing = false;
	}
}
