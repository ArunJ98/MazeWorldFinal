import java.awt.Color;
import javalib.worldimages.Posn;

class Player extends Vertex {
  boolean finished;
  int wrongMoves;

  Player(Posn pos) {
    super(pos);
    this.finished = false;
    this.wrongMoves = -1;
  }

  // moves the player
  public void move(String s) {
    if (!this.isFinished()) {
      if (s.equals("up")) {
        this.moveTo(this.top);
      }
      else if (s.equals("left")) {
        this.moveTo(this.left);
      }
      else if (s.equals("down")) {
        this.moveTo(this.bottom);
      }
      else if (s.equals("right")) {
        this.moveTo(this.right);
      }
    }
  }

  // moves player to give vertex
  public void moveTo(Vertex v) {
    if (!v.onPath) {
      wrongMoves++;
    }

    this.pos = v.pos;
    this.top = v.top;
    this.left = v.left;
    this.bottom = v.bottom;
    this.right = v.right;
  }

  // sets the player to finished
  // EFFECT: modifies this players finished field to true
  public void finish() {
    this.finished = true;
  }

  // checks if the player is finished
  public boolean isFinished() {
    return this.finished;
  }

  // computes the color for the player
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