import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javalib.impworld.WorldScene;
import javalib.worldimages.AboveImage;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldImage;
import tester.Tester;

public class MazeWorldExamples {
  Comparator<Edge> compWeight = new CompareByWeight();
  Posn pos00 = new Posn(0, 0);
  Posn pos01 = new Posn(0, 1);
  Posn pos10 = new Posn(1, 0);
  Posn pos11 = new Posn(1, 1);

  Edge edge1 = new Edge(pos00, pos01, 10);
  Edge edge2 = new Edge(pos01, pos11, 10);
  Edge edge3 = new Edge(pos00, pos10, 7);

  Vertex v1 = new Vertex(pos00);
  Vertex v1A = new Vertex(pos00);
  Vertex v2 = new Vertex(pos01);
  Vertex v3 = new Vertex(pos11);
  Vertex v4 = new Vertex(pos10);

  HashMap<Posn, Posn> representatives = new HashMap<Posn, Posn>();
  List<Edge> edgesInTree = new ArrayList<Edge>();
  List<Edge> worklist = new ArrayList<Edge>();
  ArrayList<Vertex> arrVer = new ArrayList<Vertex>();
  ArrayList<ArrayList<Vertex>> vertices = new ArrayList<ArrayList<Vertex>>();

  MazeWorld m = new MazeWorld(25, 25);

  Player player1 = new Player(pos00);

  void init() {
    this.m = new MazeWorld(25, 25);
    this.representatives = m.representatives;
    this.edgesInTree = m.edgesInTree;
    this.worklist = m.worklist;
    this.vertices = m.vertices;
    this.pos00 = new Posn(0, 0);
    this.pos01 = new Posn(0, 1);
    this.pos10 = new Posn(1, 0);
    this.pos11 = new Posn(1, 1);
    this.edge1 = new Edge(this.pos00, this.pos01, 10);
    this.edge2 = new Edge(this.pos01, this.pos11, 10);
    this.edge3 = new Edge(this.pos00, this.pos10, 7);
    this.v1 = new Vertex(this.pos00);
    this.v1A = new Vertex(this.pos00);
    this.v2 = new Vertex(this.pos01);
    this.v3 = new Vertex(this.pos11);
    this.v4 = new Vertex(this.pos10);
    this.arrVer = new ArrayList<Vertex>();
    this.player1 = new Player(this.pos00);
    this.v1.right = this.v2;
    this.v1.bottom = this.v4;
    this.v2.left = this.v1;
    this.v2.bottom = this.v3;
    this.v3.top = v2;
    this.v3.left = v4;
    this.v4.right = v3;
    this.v4.top = v1;
    this.m.makeMaze();
  }

  // Maze World Tests

  // test reset()
  void testReset(Tester t) {
    this.init();
    this.m.reset();

    t.checkExpect(m.cameFromEdge, new HashMap<Posn, Edge>());
    t.checkExpect(m.representatives, new HashMap<Posn, Posn>());
    t.checkExpect(m.edgesInTree, new ArrayList<Edge>());
    t.checkExpect(m.worklist, new ArrayList<Edge>());
    t.checkExpect(m.path, new ArrayList<Vertex>());
    t.checkExpect(m.vertices, new ArrayList<ArrayList<Vertex>>());
    t.checkExpect(m.depthWorklist, new Stack<Vertex>());
    t.checkExpect(m.breadthWorklist, new LinkedList<Vertex>());
    t.checkExpect(m.menuChoice, 0);
    t.checkExpect(m.worldTicks, 0);
    t.checkExpect(m.player, new Player(new Posn(-1, -1)));
    t.checkExpect(m.player1, new Player(new Posn(-1, -1)));
    t.checkExpect(m.player2, new Player(new Posn(-1, -1)));
    t.checkExpect(m.corridorChoice, 1.0);

  }

  // test find
  boolean testFind(Tester t) {
    init();
    this.representatives.clear();

    Posn posA = new Posn(0, 0);
    Posn posB = new Posn(0, 1);
    Posn posC = new Posn(0, 2);
    Posn posD = new Posn(0, 3);
    Posn posE = new Posn(0, 4);
    Posn posF = new Posn(0, 5);

    this.representatives.put(posA, posA);
    this.representatives.put(posB, posB);
    this.representatives.put(posC, posC);
    this.representatives.put(posD, posD);
    this.representatives.put(posE, posE);
    this.representatives.put(posF, posF);

    this.representatives.put(posC, posE);
    this.representatives.put(posD, posE);
    this.representatives.put(posB, posA);

    this.representatives.put(posB, posA);
    this.representatives.put(posF, posC);

    return t.checkExpect(m.find(this.representatives, posA), posA)
        && t.checkExpect(m.find(this.representatives, posB), posA)
        && t.checkExpect(m.find(this.representatives, posC), posE)
        && t.checkExpect(m.find(this.representatives, posE), posE)
        && t.checkExpect(m.find(this.representatives, posF), posE);
  }

  // test union
  boolean testUnion(Tester t) {
    this.init();
    this.representatives.clear();
    m.union(this.representatives, new Posn(1, 1), new Posn(1, 2));
    m.union(this.representatives, new Posn(2, 1), new Posn(1, 1));
    return t.checkExpect(this.representatives.get(new Posn(1, 1)), new Posn(1, 2))
        && t.checkExpect(this.representatives.get(new Posn(2, 1)), new Posn(1, 1));
  }

  // test depthSearch()
  void testDepthSearch(Tester t) {
    this.init();
    this.m.depthSearch();

    // Check first vertex is start
    t.checkExpect(this.m.path.get(0).equals(this.m.vertices.get(0).get(0)), true);
    // Check last vertex is end
    t.checkExpect(this.m.path.get(this.m.path.size() - 1)
        .equals(this.m.vertices.get(this.m.ROWS - 1).get(this.m.COLUMNS - 1)), true);

  }

  // test breathSearch()
  void testBreathSearch(Tester t) {
    this.init();
    this.m.depthSearch();

    // Check first vertex is start
    t.checkExpect(this.m.path.get(0).equals(this.m.vertices.get(0).get(0)), true);
    // Check last vertex is end
    t.checkExpect(this.m.path.get(this.m.path.size() - 1)
        .equals(this.m.vertices.get(this.m.ROWS - 1).get(this.m.COLUMNS - 1)), true);
  }

  // test makeMaze
  void testMakeMaze(Tester t) {
    this.init();
    this.m.makeMaze();

    // Check to make sure player positions are right
    t.checkExpect(this.m.player.pos, this.m.vertices.get(0).get(0).pos);
    t.checkExpect(this.m.player1.pos, this.m.vertices.get(0).get(0).pos);
    t.checkExpect(this.m.player2.pos,
        this.vertices.get(this.m.ROWS - 1).get(this.m.COLUMNS - 1).pos);

    // check to see vertices have been created
    for (int i = 0; i < this.m.ROWS; i++) {
      for (int j = 0; j < this.m.COLUMNS; j++) {
        t.checkExpect(this.m.vertices.get(i).get(j).pos, new Posn(j, i));

      }
    }

    // if maze is made sucessfully, should be able to solve
    this.m.depthSearch();
    // Check first vertex is start
    t.checkExpect(this.m.path.get(0).equals(this.m.vertices.get(0).get(0)), true);
    // Check last vertex is end
    t.checkExpect(this.m.path.get(this.m.path.size() - 1)
        .equals(this.m.vertices.get(this.m.ROWS - 1).get(this.m.COLUMNS - 1)), true);

  }

  // test makeScene
  void testMakeScene(Tester t) {
    this.init();

    WorldScene ws = new WorldScene(this.m.WORLD_WIDTH, this.m.WORLD_HEIGHT);
    ws.placeImageXY(new RectangleImage(this.m.WORLD_WIDTH, this.m.WORLD_HEIGHT, OutlineMode.SOLID,
        Color.DARK_GRAY), ws.width / 2, ws.height / 2);
    ws.placeImageXY(
        new RectangleImage(this.m.WORLD_WIDTH - 2 * MazeWorld.BORDER_WIDTH,
            this.m.WORLD_HEIGHT - 2 * MazeWorld.BORDER_WIDTH, OutlineMode.SOLID, Color.LIGHT_GRAY),
        ws.width / 2, ws.height / 2);

    // test every menu choice
    for (int i = 1; i < 4; i++) {
      this.m.menuChoice = i;
      ws = new WorldScene(this.m.WORLD_WIDTH, this.m.WORLD_HEIGHT);
      ws.placeImageXY(new RectangleImage(this.m.WORLD_WIDTH, this.m.WORLD_HEIGHT, OutlineMode.SOLID,
          Color.DARK_GRAY), ws.width / 2, ws.height / 2);
      ws.placeImageXY(new RectangleImage(this.m.WORLD_WIDTH - 2 * MazeWorld.BORDER_WIDTH,
          this.m.WORLD_HEIGHT - 2 * MazeWorld.BORDER_WIDTH, OutlineMode.SOLID, Color.LIGHT_GRAY),
          ws.width / 2, ws.height / 2);

      m.makeImage(ws);

      // test maze
      t.checkExpect(m.makeScene(), ws);
    }

    // test menu screen
    this.m.menuChoice = 0;
    int fontSize = 24;

    ws = new WorldScene(this.m.WORLD_WIDTH, this.m.WORLD_HEIGHT);
    ws.placeImageXY(new RectangleImage(this.m.WORLD_WIDTH, this.m.WORLD_HEIGHT, OutlineMode.SOLID,
        Color.DARK_GRAY), ws.width / 2, ws.height / 2);
    ws.placeImageXY(
        new RectangleImage(this.m.WORLD_WIDTH - 2 * MazeWorld.BORDER_WIDTH,
            this.m.WORLD_HEIGHT - 2 * MazeWorld.BORDER_WIDTH, OutlineMode.SOLID, Color.LIGHT_GRAY),
        ws.width / 2, ws.height / 2);

    TextImage header = new TextImage("Menu", fontSize * 3 / 2, Color.RED);
    TextImage dpth = new TextImage("1. Depth Search", fontSize, Color.RED);
    TextImage brdth = new TextImage("2. Breadth Search", fontSize, Color.RED);
    TextImage plyr = new TextImage("3. Manual Search", fontSize, Color.RED);
    TextImage twoPlyr = new TextImage("4. Two Player Intertwined", fontSize, Color.RED);
    TextImage corridors = new TextImage("Corridor preference: Normal ('c' to change)", fontSize,
        Color.BLUE);
    TextImage reset = new TextImage("Press 'r' to Reset", fontSize * 4 / 5, Color.BLUE);
    WorldImage menuImg = new AboveImage(header, dpth, brdth, plyr, twoPlyr, corridors, reset);
    ws.placeImageXY(menuImg, ws.width / 2, ws.height / 2);

    t.checkExpect(m.makeScene(), ws);

  }

  // test OnTick
  public void testOnTick(Tester t) {
    this.init();
    this.m.menuChoice = 0;
    this.m.onTick();
    t.checkExpect(this.m.worldTicks, 0);

    this.m.menuChoice = 1;
    this.m.onTick();
    t.checkExpect(this.m.worldTicks, 1);

    this.m.menuChoice = 2;
    this.m.onTick();
    t.checkExpect(this.m.worldTicks, 2);

    this.m.menuChoice = 3;
    this.m.onTick();
    t.checkExpect(this.m.worldTicks, 2);

    this.m.menuChoice = 4;
    this.m.onTick();
    t.checkExpect(this.m.worldTicks, 2);
  }

  // test setEndVertices() (for two player)
  public void setEndVertices(Tester t) {
    this.init();
    this.m.reset();
    this.m.onKeyEvent("4");
    Posn neg = new Posn(-1, -1);
    Vertex testV = new Vertex(neg);
    Vertex ev1 = this.m.endVertex1;
    Vertex ev2 = this.m.endVertex2;
    // test to make sure end vertices have been changed
    t.checkExpect(ev1.equals(testV), false);
    t.checkExpect(ev2.equals(testV), false);
    // test to make sure end vertices are in range
    t.checkRange(ev1.pos.x, 0, this.m.ROWS);
    t.checkRange(ev1.pos.y, 0, this.m.COLUMNS);
    t.checkRange(ev2.pos.x, 0, this.m.ROWS);
    t.checkRange(ev2.pos.y, 0, this.m.COLUMNS);
  }

  // test onKey
  public void testOnKey(Tester t) {
    this.init();
    t.checkExpect(this.m.menuChoice, 0);
    this.m.onKeyEvent("1");
    t.checkExpect(this.m.menuChoice, 1);

    this.m.onKeyEvent("r");
    t.checkExpect(this.m.menuChoice, 0);
    this.m.onKeyEvent("2");
    t.checkExpect(this.m.menuChoice, 2);

    this.m.onKeyEvent("r");
    t.checkExpect(this.m.menuChoice, 0);
    this.m.onKeyEvent("3");
    t.checkExpect(this.m.menuChoice, 3);
    t.checkExpect(this.m.player.isFinished(), false);
    this.m.player.pos = this.m.vertices.get(m.ROWS - 1).get(m.COLUMNS - 1).pos;
    this.m.onKeyEvent("foo");
    t.checkExpect(this.m.player.isFinished(), true);

    this.m.onKeyEvent("r");
    t.checkExpect(this.m.menuChoice, 0);
    this.m.onKeyEvent("4");
    t.checkExpect(this.m.menuChoice, 4);
    t.checkExpect(this.m.player1.isFinished(), false);
    t.checkExpect(this.m.player2.isFinished(), false);
    this.m.player1.pos = this.m.endVertex1.pos;
    this.m.player2.pos = this.m.endVertex2.pos;
    this.m.onKeyEvent("foo");
    t.checkExpect(this.m.player1.isFinished(), true);
    t.checkExpect(this.m.player2.isFinished(), true);

    this.m.onKeyEvent("r");
    t.checkExpect(this.m.menuChoice, 0);

    this.m.onKeyEvent("c");
    t.checkExpect(this.m.menuChoice, 0);
    t.checkExpect(this.m.corridorChoice, 2.0);
    this.m.onKeyEvent("c");
    t.checkExpect(this.m.corridorChoice, 0.5);
    this.m.onKeyEvent("c");
    t.checkExpect(this.m.corridorChoice, 1.0);

    this.m.makeMaze();
    this.m.menuChoice = 3;
    Vertex posUp = this.m.player.top;
    this.m.onKeyEvent("up");
    t.checkExpect(this.m.player.pos, posUp.pos);
    Vertex posDown = this.m.player.bottom;
    this.m.onKeyEvent("down");
    t.checkExpect(this.m.player.pos, posDown.pos);

    this.m.menuChoice = 4;
    Vertex posUp2 = this.m.player2.top;
    this.m.onKeyEvent("up");
    t.checkExpect(this.m.player2.pos, posUp2.pos);

    Vertex player2Top = this.m.player1.top;
    this.m.onKeyEvent("w");
    t.checkExpect(this.m.player1.pos, player2Top.pos);

    Vertex player2Right = this.m.player1.right;
    this.m.onKeyEvent("d");
    t.checkExpect(this.m.player1.pos, player2Right.pos);

    Vertex player2Bot = this.m.player1.bottom;
    this.m.onKeyEvent("s");
    t.checkExpect(this.m.player1.pos, player2Bot.pos);

    Vertex player2Left = this.m.player1.left;
    this.m.onKeyEvent("a");
    t.checkExpect(this.m.player1.pos, player2Left.pos);
  }

  // test reconstruct()
  void testReconstruct(Tester t) {
    this.init();
    this.m.depthSearch();
    Vertex endVer = new Vertex(new Posn(this.m.ROWS - 1, this.m.COLUMNS - 1));

    for (ArrayList<Vertex> arrVer : this.m.vertices) {
      for (Vertex v : arrVer) {
        v.onPath = false;
      }
    }

    int i = 0;
    for (ArrayList<Vertex> arrVer : this.m.vertices) {
      for (Vertex v : arrVer) {
        if (v.onPath) {
          i++;
        }
      }
    }
    t.checkExpect(i, 0);
    this.m.reconstruct(endVer);
    for (ArrayList<Vertex> arrVer : this.m.vertices) {
      for (Vertex v : arrVer) {
        if (v.onPath) {
          i++;
        }
      }
    }
    t.checkNumRange(i, this.m.ROWS + this.m.COLUMNS - 2, this.m.ROWS * this.m.COLUMNS);
  }

  // Edge Tests
  // test Edge
  public void testEdge(Tester t) {
    this.init();
    t.checkExpect(this.edge1.posA, this.pos00);
    t.checkExpect(this.edge1.posB, this.pos01);
    t.checkExpect(this.edge1.weight, 10);
    t.checkExpect(this.edge2.posA, this.pos01);
    t.checkExpect(this.edge2.posB, this.pos11);
    t.checkExpect(this.edge3.weight, 7);
  }

  // test CompareByWeight
  boolean testCompareByWeight(Tester t) {
    return t.checkExpect(compWeight.compare(edge1, edge2), 0)
        && t.checkExpect(compWeight.compare(edge1, edge3), 3)
        && t.checkExpect(compWeight.compare(edge3, edge1), -3);
  }

  // Tests for Vertex class
  // testColor
  void testColorVertex(Tester t) {
    this.init();
    t.checkExpect(this.v1.color(), new Color(100, 160, 250));
    this.v1.onPath = true;
    t.checkExpect(this.v1.color(), new Color(40, 110, 220));
  }

  // testEquals
  void testEqualsVertex(Tester t) {
    this.init();
    t.checkExpect(this.v1.equals(this.v1A), true);
    t.checkExpect(this.v1.equals(this.v2), false);
  }

  // Tests for Player class
  // testColor
  void testColorPlayer(Tester t) {
    this.init();
    t.checkExpect(this.player1.color(), Color.YELLOW);
    this.player1.finish();
    t.checkExpect(this.player1.color(), Color.WHITE);
    this.player1.finished = false;
    t.checkExpect(this.player1.color(), Color.YELLOW);
    this.player1.finished = true;
    t.checkExpect(this.player1.color(), Color.WHITE);
  }

  // testFinish
  void testFinish(Tester t) {
    this.init();
    t.checkExpect(this.player1.finished, false);
    t.checkExpect(this.player1.isFinished(), false);
    this.player1.finish();
    t.checkExpect(this.player1.isFinished(), true);
    t.checkExpect(this.player1.finished, true);
  }

  // test move()
  void testMove(Tester t) {
    this.init();
    this.player1.moveTo(this.v1);
    this.player1.move("down");
    t.checkExpect(this.player1.pos, this.pos10);
    this.player1.move("up");
    t.checkExpect(this.player1.pos, this.pos00);
    this.player1.move("right");
    t.checkExpect(this.player1.pos, this.pos01);
    this.player1.move("left");
    t.checkExpect(this.player1.pos, this.pos00);
    this.player1.move("up");
    t.checkExpect(this.player1.pos, this.pos00);
    this.player1.move("left");
    t.checkExpect(this.player1.pos, this.pos00);

  }

  // test moveTo()
  void testMoveTo(Tester t) {
    this.init();
    this.v1.onPath = false;
    t.checkExpect(this.player1.wrongMoves, -1);
    this.player1.moveTo(this.v1);
    t.checkExpect(this.player1.wrongMoves, 0);
    t.checkExpect(this.player1.pos, this.pos00);
    t.checkExpect(this.player1.top, this.v1);
    t.checkExpect(this.player1.right, this.v2);
    t.checkExpect(this.player1.left, this.v1);
    t.checkExpect(this.player1.bottom, this.v4);

    this.v2.onPath = true;
    this.player1.moveTo(this.v2);
    t.checkExpect(this.player1.wrongMoves, 0);
  }

  // test isFinished()
  void testIsFinished(Tester t) {
    this.init();
    this.player1.finished = false;
    t.checkExpect(this.player1.isFinished(), false);
    this.player1.finished = true;
    t.checkExpect(this.player1.isFinished(), true);
  }

  // Vertex tests
  // test equals
  public void testEquals(Tester t) {
    this.init();
    t.checkExpect(this.v1.equals(this.pos00), false);
    t.checkExpect(this.v1.equals(this.compWeight), false);
    t.checkExpect(this.v1.equals(this.edge1), false);
    t.checkExpect(this.v1.equals(this.v1), true);
    t.checkExpect(this.v1A.equals(this.v1), true);
    t.checkExpect(this.v1.equals(this.v1A), true);
    t.checkExpect(this.v1.equals(this.v2), false);
    t.checkExpect(this.v2.equals(this.v3), false);
    t.checkExpect(this.v3.equals(this.v1), false);
    this.v1.onPath = !this.v1.onPath;
    t.checkExpect(this.v1A.equals(this.v1), true);
  }

  // test hashcode
  public void testHashCode(Tester t) {
    this.init();
    t.checkExpect(this.v1.hashCode(), this.pos00.hashCode());
  }

  // test color
  public void testColor(Tester t) {
    this.init();
    t.checkExpect(this.v1.color(), new Color(100, 160, 250));
    t.checkExpect(this.v2.color(), new Color(100, 160, 250));
    t.checkExpect(this.v3.color(), new Color(100, 160, 250));
    this.v1.onPath = true;
    this.v2.onPath = true;
    t.checkExpect(this.v1.color(), new Color(40, 110, 220));
    t.checkExpect(this.v2.color(), new Color(40, 110, 220));
  }

  void testGame(Tester t) {
    MazeWorld m = new MazeWorld(60, 30);
    m.bigBang(m.WORLD_WIDTH, m.WORLD_HEIGHT, 0.001);
  }
}
