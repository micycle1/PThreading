# PThreading
PThreading: A framework for multithreaded drawing in [Processing](https://processing.org/).

---

The framework consists of two classes: `PThread` and `PThreadManager`. Override the `PThread` class with Processing code and then add instances of your new class to a PThreadManager; this will run the instances as threads that draw into your Processing sketch in a multithreaded manner.

## Motivation

Natively, Processing runs on a single thread -- all drawing is done within the *draw()* loop.

Processing does indeed provide a quick and dirty way to implement a simple thread with its *thread()* method, although as the [docs](https://processing.org/reference/thread_.html) describe...

> You cannot draw to the screen from a function called by thread(). Because it runs independently, the code will not be synchronized to the animation thread, causing strange or at least inconsistent results.

... leaving *thread()* suitable only for CPU-bound sketches, at best. 

In contrast, PThreading provides a way to do **multithreaded drawing** -- not just calculation -- and does so in an easy-to-use and well-synchronized manner. It exploits the fact that *PGraphics* objects can be drawn into safely from other threads.

## Setup

Download *pthreading.jar* from [releases](https://github.com/micycle1/PThreading/releases).

#### In the Processing IDE (PDE):

* Drag & drop *pthreading.jar* onto the PDE, or see the Processing [wiki](https://github.com/processing/processing/wiki/How-to-Install-a-Contributed-Library#non-processing-libraries) on how to add an external library to a Processing sketch.

#### In other Java IDEs:
* Add pthreading.jar to the classpath of your project.

## Getting Started

*The Javadoc is available [online](https://micycle1.github.io/PThreading/pthreading/package-summary.html) -- here, every method and class is thoroughly documented.*

### 1. Extending PThread

Create a new class that extends PThread. You have to override the PThread's *draw()* method; overriding *calc()* and *setup()* is optional. In *draw()*, put Processing drawing code that you wish to be executed as a thread. A very simple example is shown below:

#### PDE Example:

```
class myThread extends PThread { 
  
  private final int n;
  
  public myThread(PApplet p, int n) {
    super(p);
    this.n = n;
  }
  
  @Override
  public void draw(){
    g.ellipse(mouseX + n, mouseY, 50, 50);
  }
  
}
```

#### Non-PDE Example:

```
class myThread extends PThread { 
  
  private final int n;
  
  public myThread(PApplet p, int n) {
    super(p);
    this.n = n;
  }
  
  @Override
  public void draw(){
    g.ellipse(p.mouseX + n, p.mouseY, 50, 50); // note mouseX and mouseY are prefixed with 'p.'
  }
  
}
```

**Important**: 
* Prefix every call to a Processing **draw method** with **g** -- for example: *g.rect(10,10,10,10)* (relevant in all environments).
* Prefix every call to a Processing **variable** with **p** -- for example: *p.mousePressed* (relevant in non-PDE environments only).
* By default, *smooth()* is enabled for threads. Put *g.noSmooth();* in the thread consructor to disable anti-aliasing and thereby improve draw FPS.

### 2. Creating a PThreadManager
Create a thread manager and add threads to it. A [variety](micycle1.github.io/PThreading/pthreading/PThreadManager.html) of constructors are available. In this example the most simple constructor has been chosen. You can add multiple types of threads to a single thread manager.

```
threadManager = new PThreadManager(this);
myThread a = new myThread(this, 50);
myThread b = new myThread(this, -50);
threadManager.addThread(a, b);
```

### 3. Drawing threads
After adding a thread to a thread manager it will run immediately; however this does not mean the thread will be drawn into the sketch. To do this, call `draw()` on the thread manager.

```
void draw() {
  background(255);
  threadManager.draw();
}
```

Alternatively, you can call `threadManager.bindDraw()` once -- in `setup` for example -- to bind the `draw()` method of the thread manager to the end of the draw method of the sketch. After doing this, you no longer need to make a manual call to `threadManager.draw()` to draw threads.

---

That's it: now `a` and `b` will run as threads, drawing into the sketch. See the repo [examples](https://github.com/micycle1/PThreading/tree/master/examples) for the framework in action and more sophisticated behaviour.

## Limitations
Each thread uses the `Java2D` renderer, since this is the only renderer that allows PGraphics objects to be instantiated and drawn into from other threads. As a consequence you cannot **thread** `OPENGL` drawing, or any 3D draw functions -- note that you can still use the framework with a 3D sketch but any drawing with the PThreads is limited to 2D.
