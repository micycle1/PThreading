import pthreading.*;

PThreadManager boidManager;

Path boidPath;

public void setup() {

  size(1280, 720);

  textSize(20);
  
  colorMode(PApplet.HSB, 360, 100, 100);

  boidPath = new Path(this);

  PThreadManager mangerForPath = new PThreadManager(this);
  mangerForPath.addThread(boidPath);
  mangerForPath.bindDraw();

  boidManager = new PThreadManager(this);
  boidManager.addThread(BoidRunner.class, 4, 60, 3000, boidPath);
}

public void draw() {
  fill(0);
  rect(0, 0, 550, 80);
  fill(0, 15);
  rect(0, 0, width, height);
  boidManager.draw();
  fill(255);
  text("Sketch (main PApplet) FPS: " + round(frameRate), 10, 20);
  text("Average calc() FPS (if threads weren't capped): " + round(boidManager.getAverageCalcFPS()), 10, 40);
  text("Average draw() FPS(if threads weren't capped): " + round(boidManager.getAverageDrawFPS()), 10, 60);
}

public void mouseReleased() {
  if (mouseButton == LEFT) {
    boidPath.addNode(new PVector(mouseX, mouseY));
  }
}
