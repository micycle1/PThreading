import static processing.core.PConstants.PI;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import pthreading.PThread;

import java.util.ArrayList;

/**
 * Adapted from https://www.openprocessing.org/sketch/756411
 * 
 * @author micycle1
 */
public class Gravicubes extends PThread {

  final static int SHININESS = 20; // cube shininess
  int NUM_CUBES; // number of cubes
  final static int MIN_CUBE_SIZE = 4; // min cube size
  final static int MAX_CUBE_SIZE = 10; // max cube size
  float MIN_ROT = 0; // min cube rotation speed (set in setup)
  float MAX_ROT = 0; // max cube rotation speed (set in setup)
  final static int GRAVITY = 25000; // gravitational attraction to cursor
  final static float MIN_MIN_V = 1.8f; // min cube minimum velocity
  final static float MAX_MIN_V = 3.6f; // max cube minimum velocity
  final static int MIN_MAX_V = 5; // min cube maximum velocity
  final static int MAX_MAX_V = 10; // max cube maximum velocity
  final static float MIN_ACCEL = 0.2f; // min cube acceleration
  final static float MAX_ACCEL = 1.2f; // max cube acceleration
  final static int EXPLODE_RAD = 50; // radius of explosion that pushes drops on click
  final static int EXPLODE_STR = 70; // strength of click explosion
  final static float EXPLODE_DECAY = 0.8f; // explosion force decay
  final static float EXPLODE_FINISH = 0.01f; // set explode to 0 if current magnitude is below this
  int colour;

  ArrayList<Cube> cubes;

  public Gravicubes(PApplet p, int num_cubes) {
    super(p);
    cubes = new ArrayList<Cube>();
    NUM_CUBES = num_cubes;
    MIN_ROT = PI / 20;
    MAX_ROT = PI / 80;

    for (int i = 0; i < NUM_CUBES; i++) {
      cubes.add(new Cube(p, g));
    }
    this.colour = color(random(0, 255), random(0, 255), random(0, 255), 100);
  }

  public Gravicubes(processing.core.PApplet p) {
    super(p);
    cubes = new ArrayList<Cube>();
    NUM_CUBES = 1000;
    MIN_ROT = PI / 20;
    MAX_ROT = PI / 80;

    for (int i = 0; i < NUM_CUBES; i++) {
      cubes.add(new Cube(p, g));
    }
    colour = color(random(0, 255), random(0, 255), random(0, 255), 125);
  }


  @Override
    public void draw() {
    for (Cube c : cubes) {
      c.display();
    }
  }

  @Override
    public void calc() {
    if (p.mousePressed) {
      for (int i = 0; i < cubes.size(); i++) {
        // ignore cubes that aren't close enough to mouse
        PVector toCube = PVector.sub(cubes.get(i).pos, new PVector(p.mouseX, p.mouseY));
        float dist = toCube.mag();
        if (dist > EXPLODE_RAD) {
          continue;
        }

        // push cubes away from mouse, depending on how close they are
        float str = EXPLODE_STR * (EXPLODE_RAD - dist) / EXPLODE_RAD;
        toCube.normalize();
        toCube.mult(str);
        cubes.get(i).explodeV = toCube;
      }
    }
    for (Cube c : cubes) {
      c.update();
    }
  }

  class Cube {

    float size;
    PVector pos;
    PVector vel;
    float minV;
    float maxV;
    float rotX;
    float rotY;
    int colour;
    PVector explodeV; // force from explosions (clicks) isn't constrained
    PImage cache;

    Cube(PApplet p, PGraphics pg) {
      this.size = p.random(Gravicubes.MIN_CUBE_SIZE, Gravicubes.MAX_CUBE_SIZE);
      this.pos = new PVector(p.random(-p.width / 2, p.width / 2), p.random(-p.height / 2, p.height / 2));
      this.vel = PVector.random2D();
      this.minV = p.random(Gravicubes.MIN_MIN_V, Gravicubes.MAX_MIN_V);
      this.maxV = p.random(Gravicubes.MIN_MAX_V, Gravicubes.MAX_MAX_V);
      this.rotX = p.random(MIN_ROT, MAX_ROT);
      this.rotY = p.random(MIN_ROT, MAX_ROT);
      this.colour = color(random(0, 255), random(0, 255), random(0, 255), 100);
      this.explodeV = new PVector(0, 0);

      PGraphics temp = p.createGraphics((int) size + 1, (int) size + 1);
      temp.ellipseMode(PConstants.CORNER);
      temp.noStroke();
      temp.beginDraw();
      temp.fill(colour);
      temp.ellipse(size / 2, size / 2, this.size, this.size);
      temp.endDraw();
      cache = temp.get();
    }

    void update() {

      PVector accel = new PVector((p.mouseX), (p.mouseY)).sub(this.pos);
      float aMag = Gravicubes.GRAVITY / PApplet.sq(accel.mag());
      aMag = PApplet.constrain(aMag, Gravicubes.MIN_ACCEL, Gravicubes.MAX_ACCEL);
      accel.normalize();
      accel.mult(aMag);

      // add acceleration to velocity but constrain velocity so cubes don't go
      // shooting off into interstellar space
      this.vel.add(accel);
      float vMag = this.vel.mag();
      if (vMag < this.minV) {
        this.vel.mult(this.minV / vMag);
      } else if (vMag > this.maxV) {
        this.vel.div(vMag / this.maxV);
      }
      // EXPUROOOOOSION!!!
      if (this.explodeV.mag() > 0) {
        this.vel.add(this.explodeV);
        this.explodeV.mult(Gravicubes.EXPLODE_DECAY);
        if (this.explodeV.mag() < Gravicubes.EXPLODE_FINISH) {
          this.explodeV = new PVector(0, 0);
        }
      }
      this.pos.add(this.vel);
    }

    void display() {
      g.fill(colour);
      g.image(cache, pos.x, pos.y);
    }
  }
}
