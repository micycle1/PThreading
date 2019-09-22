package pThreading;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ClassUtils;

import processing.core.PApplet;

public class PThreadManager {

	private boolean live = true;
	private final PApplet p;
	private int targetFPS;
	private ScheduledExecutorService scheduler;
	private final ArrayList<PThread> threads;

	/**
	 * NO ARGS
	 * 
	 * @param p
	 * @param targetFPS
	 * @param threadCount
	 * @param threadClass
	 */
	public PThreadManager(PApplet p, int targetFPS, int threadCount, Class<? extends PThread> threadClass) {

		threads = new ArrayList<PThread>();
		this.p = p;
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(threadCount);

		try {
			Constructor<? extends PThread> constructor = threadClass.getDeclaredConstructor(PApplet.class); // todo
			for (int i = 0; i < threadCount; i++) {
				PThread thread = constructor.newInstance(p);
				scheduler.scheduleAtFixedRate(thread, 0, 1000000 / targetFPS, TimeUnit.MICROSECONDS);
				threads.add(thread);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
//		p.registerMethod("pre", this);
		System.out.println("size: " + threads.size());
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
	 *                    same args).
	 */
	public PThreadManager(PApplet p, int targetFPS, int threadCount, Class<? extends PThread> threadClass,
			Object... args) {

		threads = new ArrayList<PThread>();
		this.p = p;
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(threadCount);

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
				scheduler.scheduleAtFixedRate(thread, 0, 1000000 / targetFPS, TimeUnit.MICROSECONDS);
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
//		p.registerMethod("pre", this);
	}

	/**
	 * empty thread manager; user must add instances. Use #createNewThread to add
	 * threads.
	 * 
	 * @param p
	 * @param targetFPS
	 */
	public PThreadManager(PApplet p, int targetFPS) {
		threads = new ArrayList<PThread>();
		this.p = p;
		this.targetFPS = targetFPS;
		scheduler = Executors.newScheduledThreadPool(50); // TODO 50
	}

	/**
	 * Stop
	 */
	public void quitThreads() {
		scheduler.shutdown();
		scheduler = Executors.newScheduledThreadPool(threads.size());
		live = false;
	}

	/**
	 * Resume
	 */
	public void startThreads() {
		if (!live) {
			threads.forEach(thread -> {
				scheduler.scheduleAtFixedRate(thread, 0, 1000000 / targetFPS, TimeUnit.MICROSECONDS);
			});
			live = true;
		}
	}

	/**
	 * Binds to PApplet.
	 */
	public void draw() {
		threads.forEach(thread -> thread.render());
	}

	/**
	 * Return a thread (class instance) possibly to allow manual setting of params.
	 * Wpuld have to be cast by user
	 * 
	 * @return
	 */
	public PThread getThread() {
		return threads.get(0);
	}
}
