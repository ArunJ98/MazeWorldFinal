import java.awt.Color;
import javalib.worldimages.*;

class HexVertex {
  Posn pos;
  HexVertex top;
  HexVertex topLeft;
  HexVertex bottomLeft;
  HexVertex bottom;
  HexVertex bottomRight;
  HexVertex topRight;
  boolean processed;
  boolean onPath;

  HexVertex(Posn pos) {
    this.pos = pos;
    this.top = this;
    this.topLeft = this;
    this.bottomLeft = this;
    this.bottom = this;
    this.bottomRight = this;
    this.topRight = this;
    this.processed = false;
    this.onPath = false;
  }

  // to determine if two vertexes are equal
  public boolean equals(Object o) {
    if (o instanceof HexVertex) {
      HexVertex that = (HexVertex) o;
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

  // determines color of player
  public Color color() {
    if (this.onPath) {
      return new Color(40, 110, 220);
    }
    else {
      return new Color(100, 160, 250);
    }
  }
}
