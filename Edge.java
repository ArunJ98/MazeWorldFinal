import java.util.Comparator;
import javalib.worldimages.*;

//a class to represent an edge
class Edge {
  Posn posA;
  Posn posB;
  int weight;

  Edge(Posn nodeA, Posn nodeB, int weight) {
    this.posA = nodeA;
    this.posB = nodeB;
    this.weight = weight;
  }
}

// compares two edges by their weights
class CompareByWeight implements Comparator<Edge> {
  @Override
  public int compare(Edge a, Edge b) {
    return a.weight - b.weight;
  }
}
