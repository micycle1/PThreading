# PThreading
A simple multithreaded framework for Processing.

---

The framework consists of two classes: `PThread` and `PThreadManager`. Override the `PThread` class with your code and then add instances of your new class to a PThreadManager which runs the instances as threads that draw into your Processing sketch.

## Motivation

Natively, Processing runs on a single thread -- all drawing is done within the *draw()* loop.

Processing does indeed provide a quick and dirty way to implement a simple thread with its *thread()* method, although as the [docs](https://processing.org/reference/thread_.html) describe...

> You cannot draw to the screen from a function called by thread(). Because it runs independently, the code will not be synchronized to the animation thread, causing strange or at least inconsistent results.

... leaving *thread()* suitable only for CPU-bound sketches, at best. 

PThreading provides a way to do multithreaded drawing -- not just calculation -- in Processing, and does so in an easy-to-use and well-synchronized manner. It exploits the fact that PGraphics objects can be drawn into safely from other threads.

## Setup

First download *pthreading.jar* from [releases](https://github.com/micycle1/PThreading/releases).

### In the Processing IDE (PDE)

Drag & drop *pthreading.jar* onto the PDE, or see the Processing [wiki](https://github.com/processing/processing/wiki/How-to-Install-a-Contributed-Library#non-processing-libraries) on how to add an external library to a Processing sketch.

### In other Java IDEs
Add pthreading.jar to the classpath of your project.

## Getting Started

*The Javadoc is available online [here](https://micycle1.github.io/PThreading/pthreading/package-summary.html) -- every method and class is thoroughly documented.*

### 1. Extending PThread

Create a new class that extends PThread. You will have to override the PThread's *draw()* method. Here, put code you wish to be executted 
Put your code in draw(). A very simple example is shown below.

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
    g.ellipse(p.mouseX + n, p.mouseY, 50, 50);
  }
  
}
```

**Important**: 
* Prefix every call to a Processing draw method with g -- for example: *g.rect(10,10,10,10)*.
* Prefix every call to a Processing variable with p -- for example: *p.mousePressed* (non-PDE environments only).

### 2. Creating a PThreadManager

```
threadManager = new PThreadManager(this);
myThread a = new myThread(this);
threadManager.addThread(a);
```

## Limitations
Each thread uses the `Java2D` renderer, since this is the only renderer that allows PGraphics objects to be instantiated and drawn into from other threads. As a consequence you cannot **thread** `OPENGL` drawing, or any 3D draw functions -- note that you can still use the framework with a 3D sketch but any drawing with the PThreads is limited to 2D.
Not all sketches are suitable:
