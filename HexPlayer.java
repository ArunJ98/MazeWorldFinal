import java.awt.Color;
import javalib.worldimages.Posn;

class HexPlayer extends HexVertex {
  boolean finished;
  int wrongMoves;

  HexPlayer(Posn pos) {
    super(pos);
    this.finished = false;
    this.wrongMoves = -1;
  }

  // move player
  public void move(String s) {
    if (!this.isFinished()) {
      if (s.equals("w")) {
        this.moveTo(this.top);
      }
      else if (s.equals("e")) {
        this.moveTo(this.topRight);
      }
      else if (s.equals("d")) {
        this.moveTo(this.bottomRight);
      }
      else if (s.equals("x")) {
        this.moveTo(this.bottom);
      }
      else if (s.equals("z")) {
        this.moveTo(this.bottomLeft);
      }
      else if (s.equals("a")) {
        this.moveTo(this.topLeft);
      }
    }
  }

  // move player to given vertex
  public void moveTo(HexVertex v) {
    if (!v.onPath) {
      wrongMoves++;
    }

    this.pos = v.pos;
    this.top = v.top;
    this.topLeft = v.topLeft;
    this.bottomLeft = v.bottomLeft;
    this.bottom = v.bottom;
    this.bottomRight = v.bottomRight;
    this.topRight = v.topRight;
  }

  // sets finished to true
  // EFFECT: finish is set to true
  public void finish() {
    this.finished = true;
  }

  // checks if player is finished
  public boolean isFinished() {
    return this.finished;
  }

  // returns the color of the player
  @Override
  public Color color() {
    if (this.isFinished()) {
      return Color.WHITE;
    }
    else {
      return Color.YELLOW;
    }
  }
}
