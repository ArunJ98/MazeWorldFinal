import java.awt.Color;
import javalib.worldimages.*;

class Vertex {
  Posn pos;
  Vertex top;
  Vertex left;
  Vertex bottom;
  Vertex right;
  boolean processed;
  boolean onPath;

  Vertex(Posn pos) {
    this.pos = pos;
    this.top = this;
    this.left = this;
    this.bottom = this;
    this.right = this;
    this.processed = false;
    this.onPath = false;
  }

  // to determine if two vertexes are equal
  public boolean equals(Object o) {
    if (o instanceof Vertex) {
      Vertex that = (Vertex) o;
      return this.pos.equals(that.pos);
    }
    else {
      return false;
    }
  }

  // hashCode
  public int hashCode() {
    return this.pos.hashCode();
  }

  // color
  public Color color() {
    if (this.onPath) {
      return new Color(40, 110, 220);
    }
    else {
      return new Color(100, 160, 250);
    }
  }
}
