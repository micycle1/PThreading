package pthreading;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import processing.core.PApplet;

/**
 * Runs and manages {@link pthreading.PThread PThreads}.
 * <p>
 * There are a number of different constructors. Some create empty managers; in
 * others, the thread manager can be instantiated with a thread class-type from
 * which the manager will create instances of the class during instantiation.
 * 
 * <p>
 * In all cases, adding a thread to a thread manager will run it immediately.
 * 
 * @see #draw()
 * @see #addThread(PThread...)
 * @author micycle1
 *
 */
public class PThreadManager {

	private static final int DEFAULT_FPS = 60;

	private final PApplet p;
	private final int targetFPS;
	private final ScheduledExecutorService scheduler;
	private final LinkedHashMap<PThread, ScheduledFuture<?>> threads;
	private final LinkedHashMap<PThread, Integer> threadFPS;
	private boolean boundToParent = false;
	private boolean unlinkComputeDraw = false;
	private int sketchX, sketchY;

	/**
	 * Constructs a new (empty) thread manager. The simplest constructor.
	 * 
	 * @param p parent PApplet
	 */
	public PThreadManager(PApplet p) {
		this.p = p;
		threads = new LinkedHashMap<PThread, ScheduledFuture<?>>();
		threadFPS = new LinkedHashMap<PThread, Integer>();
		this.targetFPS = DEFAULT_FPS;
		scheduler = Executors.newScheduledThreadPool(50);
		p.registerMethod("dispose", this);
		sketchX = p.width;
		sketchY = p.height;
	}

	/**
	 * Constructs a new (empty) thread manager with a user-specified default FPS for
	 * new threads. When a given thread is added to the manager and a per-thread FPS
	 * is not specified, the new thread will target the FPS as given in this
	 * constructor.
	 * 
	 * @param p         parent PApplet
	 * @param targetFPS default FPS for new threads (when not specified otherwise).
	 */
	public PThreadManager(PApplet p, int targetFPS) {
		this.p = p;
		threads = new LinkedHashMap<PThread, ScheduledFuture<?>>();
		threadFPS = new LinkedHashMap<PThread, Integer>();
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(50);
		p.registerMethod("dispose", this);
		sketchX = p.width;
		sketchY = p.height;
	}

	/**
	 * Constructs a thread manager and creates live thread instances from the given
	 * thread class (using its constructor with no additional custom args).
	 * <p>
	 * Threads can still be added later using {@link #addThread(PThread...)}.
	 * 
	 * @param p           parent PApplet
	 * @param threadClass thread (with no arguments)
	 * @param threadCount Number of threads of this class to spawn
	 * @param targetFPS   FPS the threads should target
	 * @see #PThreadManager(PApplet, Class, int, int, Object...)
	 */
	public PThreadManager(PApplet p, Class<? extends PThread> threadClass, int threadCount, int targetFPS) {

		if (threadCount < 1) {
			throw new IllegalArgumentException("threadCount should be more than 0");
		}

		this.p = p;
		threads = new LinkedHashMap<PThread, ScheduledFuture<?>>();
		threadFPS = new LinkedHashMap<PThread, Integer>();
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(threadCount);
		p.registerMethod("dispose", this);
		sketchX = p.width;
		sketchY = p.height;

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
	 * Constructs a thread manager and creates running thread instances from the
	 * given thread class and given args (in contrast to
	 * {@link #addThread(Class, int, int)} which does not support ags).
	 * <p>
	 * Threads can still be added later using {@link #addThread(PThread...)}.
	 * 
	 * @param p           parent PApplet
	 * @param threadClass A thread class you have created. It should extend the
	 *                    {@link PThread} class.
	 * @param threadCount Number of threads of this class to spawn
	 * @param targetFPS   FPS the threads should target
	 * @param args        Args to pass in to the newly created threads (all threads
	 *                    will be constructed with the same args) -- don't include
	 *                    the PApplet, only your additional args.
	 * @see #PThreadManager(PApplet, Class, int, int)
	 */
	public PThreadManager(PApplet p, Class<? extends PThread> threadClass, int threadCount, int targetFPS,
			Object... args) {

		if (threadCount < 1) {
			throw new IllegalArgumentException("threadCount should be more than 0");
		}

		this.p = p;
		threads = new LinkedHashMap<PThread, ScheduledFuture<?>>();
		threadFPS = new LinkedHashMap<PThread, Integer>();
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(threadCount);
		p.registerMethod("dispose", this);
		sketchX = p.width;
		sketchY = p.height;

		Class<?>[] constructorTypes;
		Object[] completeArgs;
		int inc = 1;

		if (p.getClass() == threadClass.getDeclaredConstructors()[0].getParameterTypes()[0]
				&& p.getClass() != PApplet.class) { // PDE Workaround
			inc = 2;
			constructorTypes = new Class[args.length + inc];
			constructorTypes[0] = p.getClass();
			constructorTypes[1] = PApplet.class;
			completeArgs = new Object[args.length + inc];
			completeArgs[0] = p;
			completeArgs[1] = p;
		} else {
			constructorTypes = new Class[args.length + inc];
			constructorTypes[0] = PApplet.class;
			completeArgs = new Object[args.length + inc];
			completeArgs[0] = p;
		}

		for (int i = 0; i < args.length; i++) {
			completeArgs[i + inc] = args[i];
			if (ClassUtils.isPrimitiveWrapper(args[i].getClass())) {
				constructorTypes[i + inc] = ClassUtils.wrapperToPrimitive(args[i].getClass()); // TODO stable?
			} else {
				constructorTypes[i + inc] = args[i].getClass();
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
	 * Adds a new thread (from a {@link PThread} instance) to the manager and runs
	 * it immediately. Since FPS is not specified, threads added using this method
	 * inherit the targetFPS of the thread manager.
	 * 
	 * @param thread Thread or threads (varargs) instances to add to the manager
	 * @see #addThread(PThread, int)
	 */
	public void addThread(PThread... thread) {
		(Arrays.asList(thread)).forEach(t -> {
			if (!threads.containsKey(t)) {
				addRunnable(t);
			}
		});
	}

	/**
	 * Adds a new thread (from a {@link PThread} instance) with an associated
	 * targetFPS to the manager, and runs it immediately.
	 * 
	 * @param thread    Thread instance to add to the manager
	 * @param targetFPS Per-thread targetFPS (does not affect other threads)
	 * @see #addThread(PThread...)
	 */
	public void addThread(PThread thread, int targetFPS) {
		if (!threads.containsKey(thread)) {
			threadFPS.put(thread, targetFPS);
			addRunnable(thread);
		}
	}

	/**
	 * Adds a number of new threads to the manager using a class type (the class
	 * should extend {@link PThread}).
	 * 
	 * @param threadClass A thread class you have created. It should extend the
	 *                    {@link PThread} class.
	 * @param threadCount Number of threads of this class to spawn
	 */
	public void addThread(Class<? extends PThread> threadClass, int threadCount) {
		addThread(threadClass, threadCount, targetFPS, new Object[] {});
	}

	/**
	 * Adds a number of new threads to the manager using a class type (the class
	 * should extend {@link PThread}). This method is a no-arg version of
	 * {@link #addThread(Class, int, int, Object...)}.
	 * 
	 * @param threadClass A thread class you have created. It should extend the
	 *                    {@link PThread} class.
	 * @param threadCount Number of threads of this class to spawn
	 * @param targetFPS   FPS the threads should target
	 */
	public void addThread(Class<? extends PThread> threadClass, int threadCount, int targetFPS) {
		addThread(threadClass, threadCount, targetFPS, new Object[] {});
	}

	/**
	 * Adds a number of new threads to the manager using a class type (the class
	 * should extend {@link PThread}) and arguments.
	 * 
	 * @param threadClass A thread class you have created. It should extend the
	 *                    {@link PThread} class.
	 * @param threadCount The number of threads of this class to spawn
	 * @param targetFPS   FPS the threads should target
	 * @param args        Args to pass in to the newly created threads (all threads
	 *                    will be constructed with the same args) -- don't include
	 *                    the PApplet, only your additional args.
	 */
	public void addThread(Class<? extends PThread> threadClass, int threadCount, int targetFPS, Object... args) {

		if (threadCount < 1) {
			throw new IllegalArgumentException("threadCount should be more than 0");
		}

		Class<?>[] constructorTypes;
		Object[] completeArgs;
		int inc = 1;

		if (p.getClass() == threadClass.getDeclaredConstructors()[0].getParameterTypes()[0]
				&& p.getClass() != PApplet.class) { // PDE Workaround
			inc = 2;
			constructorTypes = new Class[args.length + inc];
			constructorTypes[0] = p.getClass();
			constructorTypes[1] = PApplet.class;
			completeArgs = new Object[args.length + inc];
			completeArgs[0] = p;
			completeArgs[1] = p;
		} else {
			constructorTypes = new Class[args.length + inc];
			constructorTypes[0] = PApplet.class;
			completeArgs = new Object[args.length + inc];
			completeArgs[0] = p;
		}

		for (int i = 0; i < args.length; i++) {
			completeArgs[i + inc] = args[i];
			if (ClassUtils.isPrimitiveWrapper(args[i].getClass())) {
				constructorTypes[i + inc] = ClassUtils.wrapperToPrimitive(args[i].getClass()); // TODO stable?
			} else {
				constructorTypes[i + inc] = args[i].getClass();
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
	 * Pause a given thread or threads (varargs). Note that paused threads will
	 * continue to be drawn in their paused state.
	 * 
	 * @param thread The thread (or threads) to pause
	 * @see #pauseAndClearThread(PThread...)
	 * @see #pauseThreads()
	 * @see #resumeThread(PThread...)
	 */
	public void pauseThread(PThread... thread) {
		ArrayList<PThread> pauseThreads = new ArrayList<PThread>(Arrays.asList(thread));
		if (threads.keySet().containsAll(pauseThreads)) {
			pauseThreads.forEach(t -> threads.get(t).cancel(true));
		}
	}

	/**
	 * Pause a given thread or threads (varargs) and clear so it will not draw
	 * visible on screen.
	 * 
	 * @param thread The thread(s) to pause
	 * @see #pauseThread(PThread...)
	 * @see #pauseAndClearThreads()
	 * @see #resumeThread(PThread...)
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
	 * Pauses all threads. Note that paused threads will continue to be drawn in
	 * their paused state.
	 * 
	 * @see #resumeThreads()
	 * @see #pauseAndClearThreads()
	 */
	public void pauseThreads() {
		threads.values().forEach(runnable -> runnable.cancel(true));
	}

	/**
	 * Pauses all threads and clears them from being drawn.
	 * 
	 * @see #resumeThreads()
	 * @see #pauseThreads()
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
	 * @see #pauseThread(PThread...)
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
	 * Resumes any and all paused threads.
	 * 
	 * @see #pauseThreads()
	 * @see #pauseAndClearThreads()
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
	 * @param thread The thread (or threads) to stop indefinitely
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

		if (sketchX != p.width || sketchY != p.height) {
			pauseThreads(); // TODO add delay?
			threads.keySet().forEach(t -> t.resize());
			resumeThreads();
		}
		sketchX = p.width;
		sketchY = p.height;

		if (unlinkComputeDraw) {
			threads.forEach((thread, runnable) -> {
				if (!runnable.isCancelled()) {
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
	 * sequentially within the thread. <i>But what if a given thread is severely
	 * draw-call bound?</i> When this function is called, all threads' (both
	 * existing and future) <i>calc()</i> method will be called as part of the
	 * thread manager's {@link #draw()} method and not within the threads. In other
	 * words, draw-intensive sketches (threads) will not slow down the
	 * processing/calculation speed of a thread.
	 */
	public void unlinkComputeDraw() {
		if (!unlinkComputeDraw) {
			unlinkComputeDraw = true;
			Timer timer = new Timer(20, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					threads.forEach((thread, value) -> {
						if (!value.isCancelled()) {
							threads.get(thread).cancel(true);
							addRunnable(thread);
						}
					});
				}
			});
			timer.setRepeats(false);
			timer.start();
		}
	}

	/**
	 * Instructs the thread manager that both existing and future threads' draw()
	 * and calc() methods are to be called sequentially (default behaviour).
	 * 
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
	 * Returns the count of the threads (both paused and running) managed by this
	 * thread manager.
	 * 
	 * @return thread count
	 */
	public int getThreadCount() {
		return threads.size();
	}

	/**
	 * First enables timing collection for each thread, then returns the average FPS
	 * of running threads' draw() loops.
	 * 
	 * @return mean draw() FPS of all threads
	 * @see #getAverageCalcFPS()
	 */
	public float getAverageDrawFPS() {
		int count = 0;
		float sum = 0;
		for (PThread thread : threads.keySet()) {
			if (!threads.get(thread).isCancelled()) {
				count++;
				sum += thread.getDrawFPS();
			}
		}
		return count > 0 ? sum / count : 0;
	}

	/**
	 * First enables timing collection for each thread, then returns the average FPS
	 * of running threads' calc() loops.
	 * 
	 * @return mean calc() FPS of all threads
	 * @see #getAverageDrawFPS()
	 */
	public float getAverageCalcFPS() {
		int count = 0;
		float sum = 0;
		for (PThread thread : threads.keySet()) {
			if (!threads.get(thread).isCancelled()) {
				count++;
				sum += thread.getCalcFPS();
			}
		}
		return count > 0 ? sum / count : 0;
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
		thread.internalSetup();
		if (unlinkComputeDraw) {
			scheduledRunnable = scheduler.scheduleAtFixedRate(thread.noCalc, 0,
					1000000 / (threadFPS.containsKey(thread) ? threadFPS.get(thread) : targetFPS),
					TimeUnit.MICROSECONDS);
		} else {
			scheduledRunnable = scheduler.scheduleAtFixedRate(thread.r, 0,
					1000000 / (threadFPS.containsKey(thread) ? threadFPS.get(thread) : targetFPS),
					TimeUnit.MICROSECONDS);
		}
		threads.put(thread, scheduledRunnable);
	}

	/**
	 * org.apache.commons.lang3.ClassUtils;
	 */
	private static final class ClassUtils {
		private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
		private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();
		static {
			primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
			primitiveWrapperMap.put(Byte.TYPE, Byte.class);
			primitiveWrapperMap.put(Character.TYPE, Character.class);
			primitiveWrapperMap.put(Short.TYPE, Short.class);
			primitiveWrapperMap.put(Integer.TYPE, Integer.class);
			primitiveWrapperMap.put(Long.TYPE, Long.class);
			primitiveWrapperMap.put(Double.TYPE, Double.class);
			primitiveWrapperMap.put(Float.TYPE, Float.class);
			primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
			for (final Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperMap.entrySet()) {
				final Class<?> primitiveClass = entry.getKey();
				final Class<?> wrapperClass = entry.getValue();
				if (!primitiveClass.equals(wrapperClass)) {
					wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
				}
			}
		}

		private static Class<?> wrapperToPrimitive(final Class<?> cls) {
			return wrapperPrimitiveMap.get(cls);
		}

		private static boolean isPrimitiveWrapper(final Class<?> type) {
			return wrapperPrimitiveMap.containsKey(type);
		}
	}
}