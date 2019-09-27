package pthreading;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ClassUtils;

import processing.core.PApplet;

/**
 * TODO getTiming() This is what threads the classes.
 * 
 * There are two types of constructor for the thread manager: pass instances of
 * your class that extends a {@link pthreading.PThread PThread}; or pass the
 * class & number of threads and the thread manager will create 'threads' number
 * of instances
 * 
 * @author micycle1
 *
 */
public class PThreadManager {

	private static final int DEFAULT_FPS = 60;

	private final PApplet p;
	private boolean live;
	private final int targetFPS;
	private ScheduledExecutorService scheduler;
	private final ArrayList<PThread> threads;
	private boolean boundToParent = false;

	/**
	 * NO class ARGS
	 * 
	 * @param p
	 * @param targetFPS
	 * @param threadCount
	 * @param threadClass
	 */
	public PThreadManager(PApplet p, int targetFPS, int threadCount, Class<? extends PThread> threadClass) {

		if (threadCount < 1) {
			throw new IllegalArgumentException("threadCount should be more than 0");
		}

		this.p = p;
		threads = new ArrayList<PThread>();
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(threadCount);
		p.registerMethod("dispose", this);

		try {
			Constructor<? extends PThread> constructor = threadClass.getDeclaredConstructor(PApplet.class); // todo
			for (int i = 0; i < threadCount; i++) {
				PThread thread = constructor.newInstance(p);
				scheduler.scheduleAtFixedRate(thread.r, 0, 1000000 / targetFPS, TimeUnit.MICROSECONDS);
				threads.add(thread);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		live = true;
	}

	/**
	 * Construct a thread manager of a given class
	 * 
	 * @param p
	 * @param targetFPS
	 * @param threadCount
	 * @param threadClass The thread class you have implemented. It should extend
	 *                    the {@link proThread.PThread PThread} class;
	 * @param args        Args to pass in (all threads will be constructed with the
	 *                    same args) -- don't include the PApplet, only your custom
	 *                    args.
	 */
	public PThreadManager(PApplet p, int targetFPS, int threadCount, Class<? extends PThread> threadClass,
			Object... args) {

		if (threadCount < 1) {
			throw new IllegalArgumentException("threadCount should be more than 0");
		}

		this.p = p;
		threads = new ArrayList<PThread>();
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(threadCount);
		p.registerMethod("dispose", this);

		final Class<?>[] constructorTypes = new Class[args.length + 1];
		constructorTypes[0] = PApplet.class;
		final Object[] completeArgs = new Object[args.length + 1];
		completeArgs[0] = p;

		for (int i = 0; i < args.length; i++) {
			completeArgs[i + 1] = args[i];
			if (ClassUtils.isPrimitiveWrapper(args[i].getClass())) {
				constructorTypes[i + 1] = ClassUtils.wrapperToPrimitive(args[i].getClass()); // TODO stable?
			} else {
				constructorTypes[i + 1] = args[i].getClass();
			}
		}

		try {
			Constructor<? extends PThread> constructor = threadClass.getDeclaredConstructor(constructorTypes);
			for (int i = 0; i < threadCount; i++) {
				PThread thread = constructor.newInstance(completeArgs);
				scheduler.scheduleAtFixedRate(thread.r, 0, 1000000 / targetFPS, TimeUnit.MICROSECONDS);
				threads.add(thread);
			}

		} catch (NoSuchMethodException e) {
			System.err.println(
					"NoSuchMethodException. If there are non-primitive arguments (i.e. Integer, not int) in your thread class constructor, then this is the cause. Otherwise, the args you specified do not match a constructor from the class.");
			e.printStackTrace();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			e.printStackTrace();
		}
		live = true;
	}

	/**
	 * No FPS, use defautk fps
	 * 
	 * @param p
	 */
	public PThreadManager(PApplet p) {
		this.p = p;
		threads = new ArrayList<PThread>();
		this.targetFPS = DEFAULT_FPS;
		scheduler = Executors.newScheduledThreadPool(50); // TODO 50
		p.registerMethod("dispose", this);
		live = false;
	}

	/**
	 * empty thread manager; user must add instances. Use #createNewThread to add
	 * threads. Specify an FPS; if not, then specifiy per-thread; if not that, then
	 * will use default (60)
	 * 
	 * @param p
	 * @param targetFPS
	 */
	public PThreadManager(PApplet p, int targetFPS) {
		this.p = p;
		threads = new ArrayList<PThread>();
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(50); // TODO 50
		p.registerMethod("dispose", this);
		live = false;
	}

	/**
	 * Add a new thread. Inherits the . Create a new thread from an instantiated
	 * PThread object. Runs immediately.
	 * 
	 * Optional if you used first 2 constructors; necessary if you used
	 * {@link #PThreadManager(PApplet, int) this} one.
	 * 
	 * This thread will inheirit {@link #targetFPS target fps}.
	 * 
	 * @see #addThread(PThread, int)
	 */
	public void addThread(PThread thread) {
		threads.add(thread);
		scheduler.scheduleAtFixedRate(thread.r, 0, 1000000 / targetFPS, TimeUnit.MICROSECONDS);
		live = true;
	}

	/**
	 * Create a new thread from an instantiated PThread object. Runs immediately.
	 * 
	 * User defines per-thread targetFPS (does not affect other threads).
	 * 
	 * @see #addThread(PThread)
	 */
	public void addThread(PThread thread, int targetFPS) {
		threads.add(thread);
		scheduler.scheduleAtFixedRate(thread.r, 0, 1000000 / targetFPS, TimeUnit.MICROSECONDS);
		live = true;
	}

	/**
	 * Removes and terminates a given thread from the thread manager. Will not
	 * return an error if the thread is not found.
	 * 
	 * @param thread the thread to remove
	 */
	public void removeThread(PThread thread) {
		pauseThreads();
		threads.remove(thread);
		resumeThreads();
	}

	/**
	 * Stops and Doesn't clear threads from the buffer. Threads will continue to
	 * render there
	 */
	public void pauseThreads() {
		scheduler.shutdown();
		scheduler = Executors.newScheduledThreadPool(threads.size());
		live = false;
	}

	/**
	 * Stops threads -- cannot be resumed, effectively resetting the thread manager.
	 */
	public void pauseAndClearThreads() {
		threads.forEach(thread -> thread.clearPGraphics());
		scheduler.shutdown();
		scheduler = Executors.newScheduledThreadPool(threads.size());
		live = false;
	}

	/**
	 * Resume
	 */
	public void resumeThreads() {
		if (!live) {
			threads.forEach(thread -> {
				scheduler.scheduleAtFixedRate(thread.r, 0, 1000000 / targetFPS, TimeUnit.MICROSECONDS);
			});
			live = true;
		}
	}

	/**
	 * Stops any existing threads and clears the internal buffer (can't be resumed).
	 * 
	 * @see #reset()
	 */
	public void flush() {
		scheduler.shutdown();
		scheduler = Executors.newScheduledThreadPool(threads.size());
		threads.clear(); // important
	}

	/**
	 * reset/restart the state of threads (remakes them).
	 */
	public void reset() {
		// TODO
	}

	/**
	 * Draw the threads' PGraphics.
	 * 
	 * @see #bindDraw()
	 */
	public void draw() {
		threads.forEach(t -> {
			p.image(t.pthread, 0, 0);
		});
	}

	/**
	 * Return a thread (class instance) possibly to allow manual setting of params.
	 * Wpuld have to be cast by user
	 * 
	 * @return
	 */
	public PThread getThread() { // TODO
		return threads.get(0);
	}

	/**
	 * Advanced: By default, a PThread's draw() and calc() are called sequentially.
	 * What if When this function is enabled, all threads' calc() is called as part
	 * of {@link #draw()}, not in the thread themselves -- so draw--intensive
	 * sketches (threads) will not slow down the speed.
	 */
	public void unlinkComputeDraw() {
		// TODO change flag
	}

	/**
	 * Retuns the count of the threads in the buffer.
	 * 
	 * @return
	 */
	public int getThreadCount() {
		return threads.size();
	}

	/**
	 * First enables timing collection for each thread.
	 * 
	 * <p>[1000/{@link #getAverageDrawTime()}] returns FPS.<p>
	 * 
	 * @return
	 */
	public float getAverageDrawTime() {
		if (!threads.isEmpty()) {
			threads.forEach(thread -> thread.enableTiming());
			return (float) threads.stream().mapToDouble(thread -> thread.drawTime).sum() / threads.size();
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * @return
	 */
	public float getAverageCalcTime() {
		if (!threads.isEmpty()) {
			threads.forEach(thread -> thread.enableTiming());
			return (float) threads.stream().mapToDouble(thread -> thread.calcTime).sum() / threads.size();
		} else {
			return 0;
		}
	}

	/**
	 * Is the thread manager running any threads? If {@link #pauseThreads()} has
	 * been called, this method will return false.
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return live;
	}

	/**
	 * This method is bound to the PApplet, so that the threads are terminated
	 * properly when the sketch is closed. You do not need to call it manually.
	 */
	public void dispose() {
		scheduler.shutdown();
	}

	/**
	 * Binds the {@link #draw()} method of this thread manager to the end of the
	 * draw method of the parent PApplet, so you don't need to manually include a
	 * call to {@link #draw() myManger.draw()} in the PApplet's draw() method. By
	 * default,
	 * 
	 * @see #unBindDraw()
	 */
	public void bindDraw() {
		if (!boundToParent) {
			p.registerMethod("draw", this);
			boundToParent = true;
		}
	}

	/**
	 * Binds the {@link #draw()} method of this thread manager (this is the default
	 * behaviour).
	 * 
	 * @see #bindDraw()
	 */
	public void unBindDraw() {
		if (boundToParent) {
			p.unregisterMethod("draw", this);
		}
	}
}
