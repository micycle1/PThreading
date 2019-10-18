import processing.core.PApplet;
import pthreading.PThreadManager;

/**
 * PThreading Gravicubes Example
 * 
 * @author micycle1
 *
 */

PThreadManager manager;
Gravicubes x, y;

boolean frozen = false;

void settings() {
  size(800, 800);
}

void setup() {
  manager = new PThreadManager(this, Gravicubes.class, 2, 60, 1000); // instantiate manager with 2 threads
  
  manager.addThread(Gravicubes.class, 2, 60, 2000); // create two threads using Gravicubes class

  x = new Gravicubes(this, 5000); // create a fifth thread (instance)
  y = new Gravicubes(this, 5000); // create a sixth thread (instance)
  manager.addThread(x); // add thread instance to manager to run it
  manager.addThread(y); // add thread instance to manager to run it
}

void draw() {
  fill(255, 100);
  rect(0, 0, width, height);
  manager.draw();
}

void mousePressed() {
  frozen = !frozen;
  if (frozen) {
    manager.pauseThread(y);
    manager.resumeThread(x);
  } else {
    manager.pauseThread(x);
    manager.resumeThread(y);
  }
}

void keyPressed() {
  if (key == 'A') {
    manager.unlinkComputeDraw();
  }
  if (key == 'S') {
    manager.relinkComputeDraw();
  }
  manager.resumeThreads();
}
