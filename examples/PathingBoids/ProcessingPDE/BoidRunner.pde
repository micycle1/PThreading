public class BoidRunner extends PThread {

  private ArrayList<Boid> boids;
  private int fillColor;

  public BoidRunner(PApplet p, int n, Path path) {
    super(p);
    boids = new ArrayList<Boid>();
    for (int i = 0; i < n; i++) {
      boids.add(new Boid(p, g, new PVector((int) random(0, width), (int) random(0, height)), path));
    }
    g.noSmooth();
  }

  @Override
    protected void setup() {
    g.colorMode(PApplet.HSB, 360, 100, 100);
    g.strokeWeight(3);
    fillColor = color(((frameCount / 10) + (int) random(0, 360)) % 360, 100, 100);
  }

  @Override
    protected void draw() {
    for (Boid b : boids) {
      g.stroke(fillColor);
      //      b.position.x = PApplet.constrain(b.position.x, 0, p.width - 1);
      //      b.position.y = PApplet.constrain(b.position.y, 0, p.height - 1);
      //      g.point(b.position.x, b.position.y); // draw boid into thread PGraphics
      b.drawForces();
    }
  }

  @Override
    protected void calc() {
    for (Boid b : boids) {
      b.calc();
    }
  }
}
