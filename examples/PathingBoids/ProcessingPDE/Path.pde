public class Path extends PThread {

  private static final float speed = 0.0025f;

  private ArrayList<PVector> nodes;

  public Path(PApplet p) {
    super(p);
    nodes = new ArrayList<PVector>();
    addNode(new PVector(p.width * 0.25f, p.height / 4));
    addNode(new PVector(p.width * 0.75f, p.height / 4));
    addNode(new PVector(p.width * 0.75f, p.height * 0.75f));
    addNode(new PVector(p.width * 0.25f, p.height * 0.75f));
  }

  @Override
    public void setup() {
    g.colorMode(PConstants.HSB, 360, 100, 100);
  }

  @Override
    public void draw() {

    g.noFill();
    g.beginShape();
    g.stroke(0, 0, 100);
    g.strokeWeight(3);
    for (PVector node : nodes) {
      g.vertex(node.x, node.y);
    }
    g.vertex(nodes.get(0).x, nodes.get(0).y); // Optional, to close loop

    g.endShape();
    g.fill(0, 100, 100);
    g.noStroke();
    for (PVector node : nodes) {
      g.ellipse(node.x, node.y, 10, 10);
      g.text(nodes.indexOf(node), node.x + 10, node.y - 10);
    }
  }

  @Override
    public void calc() {
    final int xOffset = p.mouseX - (p.width / 2);
    final int yOffset = p.mouseY - (p.height / 2);

    p.noiseSeed(2394234);
    nodes.get(0).set(new PVector(
      PApplet.map(p.noise(p.frameCount * speed), 0, 1, 0 + xOffset, p.width * 0.5f + xOffset), 
      PApplet.map(p.noise(p.frameCount * speed + 1500), 0, 1, 0 + yOffset, p.height * 0.5f + yOffset)));

    p.noiseSeed(3223934);
    nodes.get(1).set(new PVector(
      PApplet.map(p.noise(p.frameCount * speed), 0, 1, p.width * 0.5f + xOffset, p.width + xOffset), 
      PApplet.map(p.noise(p.frameCount * speed + 1000), 0, 1, 0 + yOffset, p.height * 0.5f + yOffset)));

    p.noiseSeed(3394234);
    nodes.get(2)
      .set(new PVector(
      PApplet.map(p.noise(p.frameCount * speed), 0, 1, p.width * 0.5f + xOffset, p.width + xOffset), 
      PApplet.map(p.noise(p.frameCount * speed + 2500), 0, 1, p.height * 0.5f + yOffset, 
      p.height + yOffset)));
    p.noiseSeed(32276674);
    nodes.get(3)
      .set(new PVector(
      PApplet.map(p.noise(p.frameCount * speed), 0, 1, 0 + xOffset, p.width * 0.5f + xOffset), 
      PApplet.map(p.noise(p.frameCount * speed + 2000), 0, 1, p.height * 0.5f + yOffset, 
      p.height + yOffset)));

    for (int i = 4; i < nodes.size(); i++) {
      p.noiseSeed(i * 100000);
      nodes.get(i)
        .set(new PVector(PApplet.map(p.noise(p.frameCount * speed), 0, 1, 0 + xOffset, p.width + xOffset), 
        PApplet.map(p.noise(p.frameCount * speed + 1500), 0, 1, 0 + yOffset, p.height + yOffset)));
    }
  }

  public void addNode(PVector node) {
    nodes.add(node);
  }

  public ArrayList<PVector> getNodes() {
    return nodes;
  }

  public PVector getNode(int id) {
    return nodes.get(id);
  }

  public int size() {
    return nodes.size();
  }
}
