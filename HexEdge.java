import java.util.Comparator;
import javalib.worldimages.*;

// a class to represent an edge
class HexEdge {
  Posn posA;
  Posn posB;
  int weight;

  HexEdge(Posn nodeA, Posn nodeB, int weight) {
    this.posA = nodeA;
    this.posB = nodeB;
    this.weight = weight;
  }
}

// compares two edges by their weights
class HexCompareByWeight implements Comparator<HexEdge> {
  @Override
  public int compare(HexEdge a, HexEdge b) {
    return a.weight - b.weight;
  }
}
