package pthreading;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import org.apache.commons.lang3.ClassUtils;

import processing.core.PApplet;

/**
 * Runs and manages {@link pthreading.PThread PThreads}.
 * <p>
 * There are a few different constructors split into two types: pass instances
 * of your class that extends a {@link pthreading.PThread PThread}; or pass the
 * class & number of threads and the thread manager will create 'threads' number
 * of instances internally.
 * 
 * {@link #draw()} is the most important method
 * 
 * @see #draw() draw() -- important
 * @author micycle1
 *
 */
public class PThreadManager {

	private static final int DEFAULT_FPS = 60;

	private final PApplet p;
	private final int targetFPS;
	private final ScheduledExecutorService scheduler;
	private final HashMap<PThread, ScheduledFuture<?>> threads;
	private final HashMap<PThread, Integer> threadFPS;
	private boolean boundToParent = false;
	private boolean unlinkComputeDraw = false;

	/**
	 * Constructs a new (empty) thread manager. The simplest constructor.
	 * 
	 * @param p parent PApplet
	 */
	public PThreadManager(PApplet p) {
		this.p = p;
		threads = new HashMap<PThread, ScheduledFuture<?>>();
		threadFPS = new HashMap<PThread, Integer>();
		this.targetFPS = DEFAULT_FPS;
		scheduler = Executors.newScheduledThreadPool(50);
		p.registerMethod("dispose", this);
	}

	/**
	 * Constructs a new (empty) thread manager and specifies the default FPS for
	 * threads. When a given thread is added to the manager and a per-thread FPS is
	 * not specified, the new thread will target the FPS as given in this
	 * constructor.
	 * 
	 * @param p         parent PApplet
	 * @param targetFPS default FPS for new threads (when not specified otherwise).
	 */
	public PThreadManager(PApplet p, int targetFPS) {
		this.p = p;
		threads = new HashMap<PThread, ScheduledFuture<?>>();
		threadFPS = new HashMap<PThread, Integer>();
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(50);
		p.registerMethod("dispose", this);
	}

	/**
	 * Constructs a thread manager using a thread class, so that thread instances
	 * are created upon manager instantiation.
	 * 
	 * @param p           parent PApplet
	 * @param threadClass thread (with no arguments)
	 * @param threadCount
	 * @param targetFPS   default FPS for new threads (when not specified
	 *                    otherwise).
	 * @see #PThreadManager(PApplet, Class, int, int, Object...)
	 */
	public PThreadManager(PApplet p, Class<? extends PThread> threadClass, int threadCount, int targetFPS) {

		if (threadCount < 1) {
			throw new IllegalArgumentException("threadCount should be more than 0");
		}

		this.p = p;
		threads = new HashMap<PThread, ScheduledFuture<?>>();
		threadFPS = new HashMap<PThread, Integer>();
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(threadCount);
		p.registerMethod("dispose", this);

		try {
			Constructor<? extends PThread> constructor = threadClass.getDeclaredConstructor(PApplet.class); // todo
			for (int i = 0; i < threadCount; i++) {
				PThread thread = constructor.newInstance(p);
				ScheduledFuture<?> scheduledRunnable = scheduler.scheduleAtFixedRate(thread.r, 0, 1000000 / targetFPS,
						TimeUnit.MICROSECONDS);
				threads.put(thread, scheduledRunnable);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Construct a thread manager of a given class
	 * {@link #PThreadManager(PApplet, Class, int, int)}, but you specify arguments
	 * 
	 * @param p
	 * @param threadClass The thread class you have extended. It should extend the
	 *                    {@link proThread.PThread PThread} class;
	 * @param threadCount
	 * @param targetFPS
	 * @param args        Args to pass in (all threads will be constructed with the
	 *                    same args) -- don't include the PApplet, only your
	 *                    additional args.
	 * @see #PThreadManager(PApplet, Class, int, int)
	 */
	public PThreadManager(PApplet p, Class<? extends PThread> threadClass, int threadCount, int targetFPS,
			Object... args) {

		if (threadCount < 1) {
			throw new IllegalArgumentException("threadCount should be more than 0");
		}

		this.p = p;
		threads = new HashMap<PThread, ScheduledFuture<?>>();
		threadFPS = new HashMap<PThread, Integer>();
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
				ScheduledFuture<?> scheduledRunnable = scheduler.scheduleAtFixedRate(thread.r, 0, 1000000 / targetFPS,
						TimeUnit.MICROSECONDS);
				threads.put(thread, scheduledRunnable);
			}

		} catch (NoSuchMethodException e) {
			System.err.println(
					"NoSuchMethodException. If there are non-primitive arguments (i.e. Integer, not int) in your thread class constructor, then this is the cause. Otherwise, the args you specified do not match a constructor from the class.");
			e.printStackTrace();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new thread to the manager and runs it. Inherits the targetFPS of this
	 * thread manager. Create a new thread from an instantiated PThread object. Runs
	 * immediately.
	 * 
	 * Optional if you used first 2 constructors; necessary if you used
	 * {@link #PThreadManager(PApplet, int) this} one.
	 * 
	 * This thread will inheirit {@link #targetFPS target fps}.
	 * 
	 * @see #addThread(PThread, int)
	 */
	public void addThread(PThread thread) {
		if (!threads.containsKey(thread)) {
			addRunnable(thread);
		}
	}

	/**
	 * Create a new thread from an instantiated PThread object. Runs immediately.
	 * 
	 * User defines per-thread targetFPS (does not affect other threads).
	 * 
	 * @see #addThread(PThread)
	 */
	public void addThread(PThread thread, int targetFPS) {
		if (!threads.containsKey(thread)) {
			threadFPS.put(thread, targetFPS);
			addRunnable(thread);
		}
	}

	/**
	 * TODO
	 * 
	 * @param threadClass
	 * @param threadCount
	 */
	public void addThread(Class<? extends PThread> threadClass, int threadCount) {
		addThread(threadClass, threadCount, targetFPS, new Object[] {});
	}

	/**
	 * A no arg version of {@link #addThread(Class, int, int, Object...)}
	 * 
	 * @param threadClass
	 * @param threadCount
	 * @param targetFPS
	 */
	public void addThread(Class<? extends PThread> threadClass, int threadCount, int targetFPS) {
		addThread(threadClass, threadCount, targetFPS, new Object[] {});
	}

	/**
	 * Create threads using their belonging class and args. Add a thread from a
	 * class -- the threadmanager will create threadCount instances of it.
	 * 
	 * @param threadClass
	 * @param threadCount
	 * @param targetFPS
	 * @param args        varargs exlude PApplet.
	 */
	public void addThread(Class<? extends PThread> threadClass, int threadCount, int targetFPS, Object... args) {

		if (threadCount < 1) {
			throw new IllegalArgumentException("threadCount should be more than 0");
		}

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
				threadFPS.put(thread, targetFPS);
				addRunnable(thread);
			}
		} catch (NoSuchMethodException e) {
			System.err.println(
					"NoSuchMethodException. If there are non-primitive arguments (i.e. Integer, not int) in your thread class constructor, then this is the cause. Otherwise, the args you specified do not match a constructor from the class.");
			e.printStackTrace();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException
				| InstantiationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pause a given thread or threads (varargs).
	 * 
	 * @param The thread (or threads) to pause
	 * @see #pauseAndClearThread(PThread...)
	 */
	public void pauseThread(PThread... thread) {
		ArrayList<PThread> pauseThreads = new ArrayList<PThread>(Arrays.asList(thread));
		if (threads.keySet().containsAll(pauseThreads)) {
			pauseThreads.forEach(t -> threads.get(t).cancel(true));
		}
	}

	/**
	 * Pause a given thread or threads (varargs).
	 * 
	 * @param thread(s) to pause
	 * @see #pauseThread(PThread...)
	 */
	public void pauseAndClearThread(PThread... thread) {
		ArrayList<PThread> pauseThreads = new ArrayList<PThread>(Arrays.asList(thread));
		if (threads.keySet().containsAll(pauseThreads)) {
			pauseThreads.forEach(t -> threads.get(t).cancel(true));
			Timer timer = new Timer(20, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pauseThreads.forEach(t -> t.clearPGraphics());
				}
			});
			timer.setRepeats(false);
			timer.start();
		}
	}

	/**
	 * Pauses execution of all threads -- they will still be drawn to the screen in
	 * their paused state. Stops and Doesn't clear threads from the buffer. Threads
	 * will continue to render there
	 * 
	 * @see #resumeThreads()
	 * @see #pauseAndClearThreads()
	 */
	public void pauseThreads() {
		threads.values().forEach(runnable -> runnable.cancel(true));
	}

	/**
	 * Pauses threads and hides from them being drawn.
	 */
	public void pauseAndClearThreads() {
		threads.values().forEach(runnable -> runnable.cancel(true));
		Timer timer = new Timer(20, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				threads.keySet().forEach(thread -> thread.clearPGraphics());
			}
		});
		timer.setRepeats(false);
		timer.start();
	}

	/**
	 * Resumes a given thread or threads (varargs).
	 * 
	 * @param thread The thread (or threads) to resume
	 */
	public void resumeThread(PThread... thread) {
		ArrayList<PThread> resumeThreads = new ArrayList<PThread>(Arrays.asList(thread));
		if (threads.keySet().containsAll(resumeThreads)) {
			for (PThread pThread : resumeThreads) {
				if (threads.get(pThread).isCancelled()) {
					addRunnable(pThread);
				}
			}
		}
	}

	/**
	 * Resumes any paused threads.
	 */
	public void resumeThreads() {
		for (PThread thread : threads.keySet()) {
			if (threads.get(thread).isCancelled()) {
				addRunnable(thread);
			}
		}
	}

	/**
	 * Stops a given thread or threads (varargs) and removes it from the thread
	 * manager (cannot be resumed).
	 * 
	 * @param thread The thread (or threads) to stop
	 */
	public void stopThread(PThread... thread) {
		ArrayList<PThread> stopThreads = new ArrayList<PThread>(Arrays.asList(thread));
		if (threads.keySet().containsAll(stopThreads)) {
			stopThreads.forEach(t -> threads.get(t).cancel(true));
			stopThreads.forEach(t -> threads.remove(t));
			stopThreads.forEach(t -> threadFPS.remove(t));
		}
	}

	/**
	 * Stops all threads (even if they are paused) and removes them from the thread
	 * manager, effectively resetting the thread manager.
	 */
	public void stopThreads() {
		threads.values().forEach(runnable -> runnable.cancel(true));
		threads.clear();
		threadFPS.clear();
	}

	/**
	 * Draws the threads' PGraphics into the parent PApplet. This method should be
	 * called within the parent PApplet's draw() loop -- alternatively, see
	 * {@link #bindDraw()}.
	 */
	public void draw() {
		if (unlinkComputeDraw) {
			threads.forEach((thread, value) -> {
				if (!value.isCancelled()) {
					thread.calc();
				}
				p.image(thread.g, 0, 0);
			});
		} else {
			threads.keySet().forEach(t -> {
				p.image(t.g, 0, 0);
			});
		}
	}

	/**
	 * Advanced: By default, each thread's draw() and calc() methods are called
	 * sequentially within the thread. What if a given thread is severely draw-call
	 * bound? When this function is called, all threads' (both existing and future)
	 * <i>calc()</i> method will be called as part of the thread manager's
	 * {@link #draw()} method and not within the threads: draw--intensive sketches
	 * (threads) will not slow down the speed of a thread.
	 */
	public void unlinkComputeDraw() {
		if (!unlinkComputeDraw) {
			unlinkComputeDraw = true;
			threads.forEach((thread, value) -> {
				if (!value.isCancelled()) {
					threads.get(thread).cancel(true);
					addRunnable(thread);
				}
			});	
		}
	}
	
	/**
	 * @see #unlinkComputeDraw()
	 */
	public void relinkComputeDraw() {
		if (unlinkComputeDraw) {
			unlinkComputeDraw = false;
			threads.forEach((thread, value) -> {
				if (!value.isCancelled()) {
					threads.get(thread).cancel(true);
					addRunnable(thread);
				}
			});	
		}
	}

	/**
	 * Retuns the count of the threads (both paused and running) managed by this
	 * thread manager.
	 * 
	 * @return thread count
	 */
	public int getThreadCount() {
		return threads.size();
	}

	/**
	 * First enables timing collection for each thread, then returns the average
	 * draw
	 * 
	 * <p>
	 * [1000/{@link #getAverageDrawTime()}] returns FPS.
	 * <p>
	 * 
	 * @return mean draw() time of all threads (milliseconds).
	 * @see #getAverageCalcTime()
	 */
	public float getAverageDrawTime() {
		if (!threads.isEmpty()) {
			threads.keySet().forEach(thread -> thread.enableTiming());
			return (float) threads.keySet().stream().mapToDouble(thread -> thread.drawTime).sum() / threads.size();
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * @return mean calc() time of all threads (milliseconds).
	 * @see #getAverageDrawTime()
	 */
	public float getAverageCalcTime() {
		if (!threads.isEmpty()) {
			threads.keySet().forEach(thread -> thread.enableTiming());
			return (float) threads.keySet().stream().mapToDouble(thread -> thread.calcTime).sum() / threads.size();
		} else {
			return 0;
		}
	}

	/**
	 * Is the thread manager running any threads? If {@link #pauseThreads()} has
	 * been called, this method will return false.
	 * 
	 * @return running status
	 */
	public boolean isRunning() {
		for (ScheduledFuture<?> runnable : threads.values()) {
			if (!runnable.isCancelled()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Binds the {@link #draw()} method of this thread manager to the end of the
	 * draw method of the parent PApplet, so you don't need to manually include a
	 * call to {@link #draw() myManger.draw()} in the PApplet's draw() method. By
	 * default, the thread manager is not bound to the parent PApplet -- this method
	 * must be called to set it up.
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
	 * Unbinds the {@link #draw()} method of this thread manager from the end of the
	 * draw method of the parent PApplet, so {@link #draw()} must be called
	 * manually.
	 * 
	 * @see #bindDraw()
	 */
	public void unBindDraw() {
		if (boundToParent) {
			p.unregisterMethod("draw", this);
		}
	}

	/**
	 * This method is bound to the parent PApplet, so that the threads are
	 * terminated properly when the sketch is closed. <b>You do not need to call
	 * this method manually</b>.
	 */
	public void dispose() {
		scheduler.shutdown();
	}

	/**
	 * Create a runnable for the given thread.
	 * 
	 * @param thread
	 */
	private void addRunnable(PThread thread) {
		ScheduledFuture<?> scheduledRunnable;
		if (unlinkComputeDraw) {
			scheduledRunnable = scheduler.scheduleAtFixedRate(thread.noCalc, 0,
					1000000 / (threadFPS.containsKey(thread) ? threadFPS.get(thread) : DEFAULT_FPS),
					TimeUnit.MICROSECONDS);
		} else {
			scheduledRunnable = scheduler.scheduleAtFixedRate(thread.r, 0,
					1000000 / (threadFPS.containsKey(thread) ? threadFPS.get(thread) : DEFAULT_FPS),
					TimeUnit.MICROSECONDS);
		}
		threads.put(thread, scheduledRunnable);
	}
}
