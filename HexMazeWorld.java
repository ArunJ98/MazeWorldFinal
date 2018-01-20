import java.util.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Assignment 9
//Hughes William 
//whughes
//Jeevanantham Arun
//arunj98

// a class to represent a HexMazeWorld
class HexMazeWorld extends World {
  final static int WALL_WIDTH = 4;
  final int COLUMNS;
  final int ROWS;
  final int HEX_SIDE;
  final int WORLD_WIDTH;
  final int WORLD_HEIGHT;

  HashMap<Posn, HexEdge> cameFromEdge;
  HashMap<Posn, Posn> representatives;
  ArrayList<HexEdge> edgesInTree;
  ArrayList<HexEdge> worklist;
  ArrayList<HexVertex> path;
  ArrayList<ArrayList<HexVertex>> vertices;
  Stack<HexVertex> depthWorklist;
  Queue<HexVertex> breadthWorklist;
  int menuChoice;
  int worldTicks;
  HexPlayer player;
  HexPlayer player1;
  HexPlayer player2;
  HexVertex endVertex1;
  HexVertex endVertex2;
  String winner;

  HexMazeWorld(int columns, int rows) {
    this.COLUMNS = columns;
    this.ROWS = rows;
    this.HEX_SIDE = Math.min((int) (1600 / (3 * COLUMNS + 0.5)),
        (int) (800 / ((ROWS + 1) * (Math.sqrt(3) / 2))));
    this.WORLD_WIDTH = HEX_SIDE + (int) ((3 * COLUMNS + 0.5) * HEX_SIDE);
    this.WORLD_HEIGHT = HEX_SIDE + (int) ((ROWS + 1) * (Math.sqrt(3) / 2) * HEX_SIDE);
    this.reset();
  }

  // reset
  public void reset() {
    this.cameFromEdge = new HashMap<Posn, HexEdge>();
    this.representatives = new HashMap<Posn, Posn>();
    this.edgesInTree = new ArrayList<HexEdge>();
    this.worklist = new ArrayList<HexEdge>();
    this.path = new ArrayList<HexVertex>();
    this.vertices = new ArrayList<ArrayList<HexVertex>>();
    this.depthWorklist = new Stack<HexVertex>();
    this.breadthWorklist = new LinkedList<HexVertex>();
    this.menuChoice = 0;
    this.worldTicks = 0;
    this.player = new HexPlayer(new Posn(-1, -1));
    this.player1 = new HexPlayer(new Posn(-1, -1));
    this.player2 = new HexPlayer(new Posn(-1, -1));
    this.winner = "";
  }

  // Initialize maze
  public void makeMaze() {
    // create representatives and vertices
    for (int i = 0; i < this.ROWS; i++) {
      this.vertices.add(new ArrayList<HexVertex>());

      for (int j = 0; j < this.COLUMNS; j++) {
        Posn p = new Posn(j, i);
        this.representatives.put(p, p);
        this.vertices.get(i).add(new HexVertex(p));
      }
    }

    // create worklist
    for (int i = 0; i < this.ROWS; i++) {
      for (int j = 0; j < this.COLUMNS; j++) {
        if (i % 2 == 0) {
          // bottomRight
          if (i < this.ROWS - 1) {
            this.worklist.add(new HexEdge(this.representatives.get(new Posn(j, i)),
                this.representatives.get(new Posn(j, i + 1)),
                (int) (Math.random() * this.ROWS * this.COLUMNS)));

            // bottom
            if (i < this.ROWS - 2) {
              this.worklist.add(new HexEdge(this.representatives.get(new Posn(j, i)),
                  this.representatives.get(new Posn(j, i + 2)),
                  (int) (Math.random() * this.ROWS * this.COLUMNS)));
            }
          }
        }
        else {
          // bottomLeft
          if (i < this.ROWS - 1) {
            this.worklist.add(new HexEdge(this.representatives.get(new Posn(j, i)),
                this.representatives.get(new Posn(j, i + 1)),
                (int) (Math.random() * this.ROWS * this.COLUMNS)));

            // bottom
            if (i < this.ROWS - 2) {
              this.worklist.add(new HexEdge(this.representatives.get(new Posn(j, i)),
                  this.representatives.get(new Posn(j, i + 2)),
                  (int) (Math.random() * this.ROWS * this.COLUMNS)));
            }
          }

          // topRight
          if (j < this.COLUMNS - 1) {
            this.worklist.add(new HexEdge(this.representatives.get(new Posn(j, i)),
                this.representatives.get(new Posn(j + 1, i - 1)),
                (int) (Math.random() * this.ROWS * this.COLUMNS)));

            // bottomRight
            if (i < this.ROWS - 1) {
              this.worklist.add(new HexEdge(this.representatives.get(new Posn(j, i)),
                  this.representatives.get(new Posn(j + 1, i + 1)),
                  (int) (Math.random() * this.ROWS * this.COLUMNS)));
            }
          }
        }
      }
    }

    // Sort the worklist by weight
    Collections.sort(this.worklist, new HexCompareByWeight());

    // create edgesInTree
    while (this.edgesInTree.size() < (this.ROWS * this.COLUMNS - 1)) {
      HexEdge min = this.worklist.get(0);
      if (!find(this.representatives, min.posA).equals(find(this.representatives, min.posB))) {
        this.edgesInTree.add(min);
        union(this.representatives, find(this.representatives, min.posA),
            find(this.representatives, min.posB));
      }
      this.worklist.remove(0);
    }

    // set vertices' neighbors
    for (HexEdge e : this.edgesInTree) {
      Posn pA = e.posA;
      Posn pB = e.posB;
      HexVertex vA = this.vertices.get(pA.y).get(pA.x);
      HexVertex vB = this.vertices.get(pB.y).get(pB.x);

      if (pA.x == pB.x && pA.y == pB.y - 2) {
        vA.bottom = vB;
        vB.top = vA;
      }
      else if (pA.x == pB.x && pA.y == pB.y + 2) {
        vA.top = vB;
        vB.bottom = vA;
      }

      if (pA.y % 2 == 1) {
        if (pA.x == pB.x && pA.y == pB.y - 1) {
          vA.bottomLeft = vB;
          vB.topRight = vA;
        }
        else if (pA.x == pB.x - 1 && pA.y == pB.y + 1) {
          vA.topRight = vB;
          vB.bottomLeft = vA;
        }
        else if (pA.x == pB.x && pA.y == pB.y + 1) {
          vA.topLeft = vB;
          vB.bottomRight = vA;
        }
        else if (pA.x == pB.x - 1 && pA.y == pB.y - 1) {
          vA.bottomRight = vB;
          vB.topLeft = vA;
        }
      }
      else {
        if (pA.x == pB.x + 1 && pA.y == pB.y - 1) {
          vA.bottomLeft = vB;
          vB.topRight = vA;
        }
        else if (pA.x == pB.x && pA.y == pB.y + 1) {
          vA.topRight = vB;
          vB.bottomLeft = vA;
        }
        else if (pA.x == pB.x + 1 && pA.y == pB.y + 1) {
          vA.topLeft = vB;
          vB.bottomRight = vA;
        }
        else if (pA.x == pB.x && pA.y == pB.y - 1) {
          vA.bottomRight = vB;
          vB.topLeft = vA;
        }
      }
    }
    this.player.moveTo(this.vertices.get(0).get(0));
    this.player1.moveTo(this.vertices.get(0).get(0));
    this.player2.moveTo(this.vertices.get(ROWS - 1).get(COLUMNS - 1));
  }

  // find
  public Posn find(HashMap<Posn, Posn> reps, Posn key) {
    if (reps.get(key).equals(key)) {
      return key;
    }
    else {
      return find(reps, reps.get(key));
    }
  }

  // unions two representatives
  public void union(HashMap<Posn, Posn> reps, Posn a, Posn b) {
    reps.put(a, b);
  }

  // sets end vertices for two player
  public void setEndVertices() {
    int pathLength = 1;
    int i = 1;

    for (HexVertex v : this.path) {
      if (v.onPath) {
        pathLength++;
      }
    }

    for (HexVertex v : this.path) {
      if (v.onPath) {
        i++;
        if (i == pathLength / 2) {
          this.endVertex1 = v;
        }
        if (i == pathLength / 2 + 1) {
          this.endVertex2 = v;
        }
      }
    }

    if (this.endVertex1.top.equals(endVertex2)) {
      this.endVertex1.top = this.endVertex1;
      this.endVertex2.bottom = this.endVertex2;
    }
    else if (this.endVertex1.topLeft.equals(endVertex2)) {
      this.endVertex1.topLeft = this.endVertex1;
      this.endVertex2.bottomRight = this.endVertex2;
    }
    else if (this.endVertex1.bottomLeft.equals(endVertex2)) {
      this.endVertex1.bottomLeft = this.endVertex1;
      this.endVertex2.topRight = this.endVertex2;
    }
    else if (this.endVertex1.bottom.equals(endVertex2)) {
      this.endVertex1.bottom = this.endVertex1;
      this.endVertex2.top = this.endVertex2;
    }
    else if (this.endVertex1.bottomRight.equals(endVertex2)) {
      this.endVertex1.bottomRight = this.endVertex1;
      this.endVertex2.topLeft = this.endVertex2;
    }
    else if (this.endVertex1.topRight.equals(endVertex2)) {
      this.endVertex1.topRight = this.endVertex1;
      this.endVertex2.bottomLeft = this.endVertex2;
    }
  }

  // Depth-first search
  public void depthSearch() {
    this.depthWorklist.push(this.vertices.get(0).get(0));

    while (!this.depthWorklist.isEmpty()) {
      HexVertex next = this.depthWorklist.peek();

      if (next.processed) {
        this.depthWorklist.pop();
      }
      else if (next.equals(this.vertices.get(ROWS - 1).get(COLUMNS - 1))) {
        next.processed = true;
        this.path.add(next);
        this.reconstruct(next);
        return;
      }
      else {
        next.processed = true;
        this.depthWorklist.push(next.top);
        this.depthWorklist.push(next.topLeft);
        this.depthWorklist.push(next.bottomLeft);
        this.depthWorklist.push(next.bottom);
        this.depthWorklist.push(next.bottomRight);
        this.depthWorklist.push(next.topRight);
        if (!next.top.processed) {
          this.cameFromEdge.put(next.top.pos, new HexEdge(next.pos, next.top.pos, 0));
        }
        if (!next.topLeft.processed) {
          this.cameFromEdge.put(next.topLeft.pos, new HexEdge(next.pos, next.topLeft.pos, 0));
        }
        if (!next.bottomLeft.processed) {
          this.cameFromEdge.put(next.bottomLeft.pos, new HexEdge(next.pos, next.bottomLeft.pos, 0));
        }
        if (!next.bottom.processed) {
          this.cameFromEdge.put(next.bottom.pos, new HexEdge(next.pos, next.bottom.pos, 0));
        }
        if (!next.bottomRight.processed) {
          this.cameFromEdge.put(next.bottomRight.pos,
              new HexEdge(next.pos, next.bottomRight.pos, 0));
        }
        if (!next.topRight.processed) {
          this.cameFromEdge.put(next.topRight.pos, new HexEdge(next.pos, next.topRight.pos, 0));
        }
        this.path.add(next);
      }
    }
  }

  // Breadth-first search
  public void breadthSearch() {
    this.breadthWorklist.add(this.vertices.get(0).get(0));

    while (!this.breadthWorklist.isEmpty()) {
      HexVertex next = this.breadthWorklist.peek();

      if (next.processed) {
        this.breadthWorklist.poll();
      }
      else if (next.equals(this.vertices.get(ROWS - 1).get(COLUMNS - 1))) {
        next.processed = true;
        this.path.add(next);
        this.reconstruct(next);
        return;
      }
      else {
        next.processed = true;
        this.breadthWorklist.add(next.top);
        this.breadthWorklist.add(next.topLeft);
        this.breadthWorklist.add(next.bottomLeft);
        this.breadthWorklist.add(next.bottom);
        this.breadthWorklist.add(next.bottomRight);
        this.breadthWorklist.add(next.topRight);
        if (!next.top.processed) {
          this.cameFromEdge.put(next.top.pos, new HexEdge(next.pos, next.top.pos, 0));
        }
        if (!next.topLeft.processed) {
          this.cameFromEdge.put(next.topLeft.pos, new HexEdge(next.pos, next.topLeft.pos, 0));
        }
        if (!next.bottomLeft.processed) {
          this.cameFromEdge.put(next.bottomLeft.pos, new HexEdge(next.pos, next.bottomLeft.pos, 0));
        }
        if (!next.bottom.processed) {
          this.cameFromEdge.put(next.bottom.pos, new HexEdge(next.pos, next.bottom.pos, 0));
        }
        if (!next.bottomRight.processed) {
          this.cameFromEdge.put(next.bottomRight.pos,
              new HexEdge(next.pos, next.bottomRight.pos, 0));
        }
        if (!next.topRight.processed) {
          this.cameFromEdge.put(next.topRight.pos, new HexEdge(next.pos, next.topRight.pos, 0));
        }
        this.path.add(next);
      }
    }
  }

  // reconstructs the path for the maze
  // EFFECT: sets onPath to true for vertices on solution path
  public void reconstruct(HexVertex next) {
    if (!next.equals(this.vertices.get(0).get(0))) {
      next.onPath = true;
      HexEdge toPrev = this.cameFromEdge.get(next.pos);
      Posn prevPos = toPrev.posA;
      this.reconstruct(this.vertices.get(prevPos.y).get(prevPos.x));
    }
  }

  // Create world scene
  @Override
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(WORLD_WIDTH, WORLD_HEIGHT);
    ws.placeImageXY(new RectangleImage(WORLD_WIDTH, WORLD_HEIGHT, "Solid", Color.LIGHT_GRAY),
        ws.width / 2, ws.height / 2);

    if (this.menuChoice != 0) {
      makeImage(ws);
    }
    else {
      int fontSize = 24;
      TextImage header = new TextImage("Menu", fontSize * 3 / 2, Color.RED);
      TextImage dpth = new TextImage("1. Depth Search", fontSize, Color.RED);
      TextImage brdth = new TextImage("2. Breadth Search", fontSize, Color.RED);
      TextImage plyr = new TextImage("3. Manual Search", fontSize, Color.RED);
      TextImage twoPlyr = new TextImage("4. Two Player Intertwined", fontSize, Color.RED);
      TextImage reset = new TextImage("Press 'r' to Reset", fontSize * 4 / 5, Color.BLUE);
      WorldImage menuImg = new AboveImage(header, dpth, brdth, plyr, twoPlyr, reset);
      ws.placeImageXY(menuImg, ws.width / 2, ws.height / 2);
    }

    return ws;
  }

  // Create maze image
  public void makeImage(WorldScene ws) {
    double a = HEX_SIDE * Math.sqrt(3) / 2;
    int s = HEX_SIDE * 3 / 2;
    int hexSide = this.HEX_SIDE + 1;

    // building blocks
    RectangleImage hrznWall = new RectangleImage(hexSide, WALL_WIDTH, "Solid", Color.DARK_GRAY);
    WorldImage frwdSlashWall = new RotateImage(hrznWall, -60);
    WorldImage backSlashWall = new RotateImage(hrznWall, 60);
    HexagonImage start = new HexagonImage(hexSide, "Solid", Color.GREEN.darker().darker());
    HexagonImage finish = new HexagonImage(hexSide, "Solid", new Color(200, 0, 200));
    CircleImage playerImg = new CircleImage(this.HEX_SIDE / 2, "Solid", this.player.color());
    CircleImage player1Img = new CircleImage(this.HEX_SIDE / 2, "Solid", this.player1.color());
    CircleImage player2Img = new CircleImage(this.HEX_SIDE / 2, "Solid", this.player2.color());

    // animate search for the path
    for (int i = 0; i < this.worldTicks && i < this.path.size(); i++) {
      HexVertex v = this.path.get(i);
      if (v.pos.y % 2 == 0) {
        ws.placeImageXY(new HexagonImage(hexSide, "Solid", v.color()), s + 3 * v.pos.x * HEX_SIDE,
            s + (int) ((v.pos.y / 2) * 2 * a));
      }
      else {
        ws.placeImageXY(new HexagonImage(hexSide, "Solid", v.color()),
            s + (int) ((1.5 + 3 * v.pos.x) * HEX_SIDE),
            s + (int) (((v.pos.y - 1) / 2 + 0.5) * 2 * a));
      }
    }

    // show solution when player reaches the end
    if (this.player.isFinished()) {
      for (HexVertex v : this.path) {
        if (v.onPath) {
          if (v.pos.y % 2 == 0) {
            ws.placeImageXY(new HexagonImage(hexSide, "Solid", v.color()),
                s + 3 * v.pos.x * HEX_SIDE, s + (int) ((v.pos.y / 2) * 2 * a));
          }
          else {
            ws.placeImageXY(new HexagonImage(hexSide, "Solid", v.color()),
                s + (int) ((1.5 + 3 * v.pos.x) * HEX_SIDE),
                s + (int) (((v.pos.y - 1) / 2 + 0.5) * 2 * a));
          }
        }
      }
      TextImage wrngMoves = new TextImage("You won! You had " + player.wrongMoves + " wrong moves.",
          24, Color.RED);
      ws.placeImageXY(wrngMoves, ws.width / 2, ws.height / 2);
    }

    // not two player start/end cells
    if (this.menuChoice != 4) {
      // draw start and end cells
      ws.placeImageXY(start, s, s);
      if ((ROWS - 1) % 2 == 0) {
        ws.placeImageXY(finish, s + 3 * (COLUMNS - 1) * HEX_SIDE,
            s + (int) (((ROWS - 1) / 2) * 2 * a));
      }
      else {
        ws.placeImageXY(finish, s + (int) ((1.5 + 3 * (COLUMNS - 1)) * HEX_SIDE),
            s + (int) ((((ROWS - 1) - 1) / 2 + 0.5) * 2 * a));
      }
    }

    // draw single player
    if (this.menuChoice == 3) {
      if (player.pos.y % 2 == 0) {
        ws.placeImageXY(playerImg, s + 3 * player.pos.x * HEX_SIDE,
            s + (int) ((player.pos.y / 2) * 2 * a));
      }
      else {
        ws.placeImageXY(playerImg, s + (int) ((1.5 + 3 * player.pos.x) * HEX_SIDE),
            s + (int) (((player.pos.y - 1) / 2 + 0.5) * 2 * a));
      }
    }

    // two players
    if (this.menuChoice == 4) {
      // show solution when a player reaches the end
      if (this.player1.isFinished() || this.player2.isFinished()) {
        for (HexVertex v : this.path) {
          if (v.onPath) {
            if (v.pos.y % 2 == 0) {
              ws.placeImageXY(new HexagonImage(hexSide, "Solid", v.color()),
                  s + 3 * v.pos.x * HEX_SIDE, s + (int) ((v.pos.y / 2) * 2 * a));
            }
            else {
              ws.placeImageXY(new HexagonImage(hexSide, "Solid", v.color()),
                  s + (int) ((1.5 + 3 * v.pos.x) * HEX_SIDE),
                  s + (int) (((v.pos.y - 1) / 2 + 0.5) * 2 * a));
            }
          }
        }
      }

      // draw start and end cells
      ws.placeImageXY(start, s, s);
      if ((ROWS - 1) % 2 == 0) {
        ws.placeImageXY(start, s + 3 * (COLUMNS - 1) * HEX_SIDE,
            s + (int) (((ROWS - 1) / 2) * 2 * a));
      }
      else {
        ws.placeImageXY(start, s + (int) ((1.5 + 3 * (COLUMNS - 1)) * HEX_SIDE),
            s + (int) ((((ROWS - 1) - 1) / 2 + 0.5) * 2 * a));
      }

      if (endVertex1.pos.y % 2 == 0) {
        ws.placeImageXY(finish, s + 3 * endVertex1.pos.x * HEX_SIDE,
            s + (int) ((endVertex1.pos.y / 2) * 2 * a));
      }
      else {
        ws.placeImageXY(finish, s + (int) ((1.5 + 3 * endVertex1.pos.x) * HEX_SIDE),
            s + (int) (((endVertex1.pos.y - 1) / 2 + 0.5) * 2 * a));
      }

      if (endVertex2.pos.y % 2 == 0) {
        ws.placeImageXY(finish, s + 3 * endVertex2.pos.x * HEX_SIDE,
            s + (int) ((endVertex2.pos.y / 2) * 2 * a));
      }
      else {
        ws.placeImageXY(finish, s + (int) ((1.5 + 3 * endVertex2.pos.x) * HEX_SIDE),
            s + (int) (((endVertex2.pos.y - 1) / 2 + 0.5) * 2 * a));
      }

      if (player1.pos.y % 2 == 0) {
        ws.placeImageXY(player1Img, s + 3 * player1.pos.x * HEX_SIDE,
            s + (int) ((player1.pos.y / 2) * 2 * a));
      }
      else {
        ws.placeImageXY(player1Img, s + (int) ((1.5 + 3 * player1.pos.x) * HEX_SIDE),
            s + (int) (((player1.pos.y - 1) / 2 + 0.5) * 2 * a));
      }

      if (player2.pos.y % 2 == 0) {
        ws.placeImageXY(player2Img, s + 3 * player2.pos.x * HEX_SIDE,
            s + (int) ((player2.pos.y / 2) * 2 * a));
      }
      else {
        ws.placeImageXY(player2Img, s + (int) ((1.5 + 3 * player2.pos.x) * HEX_SIDE),
            s + (int) (((player2.pos.y - 1) / 2 + 0.5) * 2 * a));
      }
    }

    // draw maze walls
    for (int i = 0; i < this.ROWS; i++) {
      for (int j = 0; j < this.COLUMNS; j++) {
        HexVertex v = vertices.get(i).get(j);

        if (v.top.equals(v)) {
          if (i % 2 == 0) {
            ws.placeImageXY(hrznWall, s + 3 * j * HEX_SIDE, (int) (s - a + (i / 2) * 2 * a));
          }
          else {
            ws.placeImageXY(hrznWall, s + (int) ((1.5 + 3 * j) * HEX_SIDE),
                (int) (s - a + ((i - 1) / 2 + 0.5) * 2 * a));
          }
        }
        if (v.topLeft.equals(v)) {
          if (i % 2 == 0) {
            ws.placeImageXY(frwdSlashWall, (int) (s - (HEX_SIDE * 0.75) + 3 * j * HEX_SIDE),
                (int) (s - (a / 2) + (i / 2) * 2 * a));
          }
          else {
            ws.placeImageXY(frwdSlashWall, (int) (s - (HEX_SIDE * 0.75) + (1.5 + 3 * j) * HEX_SIDE),
                (int) (s - (a / 2) + ((i - 1) / 2 + 0.5) * 2 * a));
          }
        }
        if (v.bottomLeft.equals(v)) {
          if (i % 2 == 0) {
            ws.placeImageXY(backSlashWall, (int) (s - (HEX_SIDE * 0.75) + 3 * j * HEX_SIDE),
                (int) (s + (a / 2) + (i / 2) * 2 * a));
          }
          else {
            ws.placeImageXY(backSlashWall, (int) (s - (HEX_SIDE * 0.75) + (1.5 + 3 * j) * HEX_SIDE),
                (int) (s + (a / 2) + ((i - 1) / 2 + 0.5) * 2 * a));
          }
        }
        if (v.bottom.equals(v)) {
          if (i % 2 == 0) {
            ws.placeImageXY(hrznWall, s + 3 * j * HEX_SIDE, (int) (s + a + (i / 2) * 2 * a));
          }
          else {
            ws.placeImageXY(hrznWall, s + (int) ((1.5 + 3 * j) * HEX_SIDE),
                (int) (s + a + ((i - 1) / 2 + 0.5) * 2 * a));
          }
        }
        if (v.bottomRight.equals(v)) {
          if (i % 2 == 0) {
            ws.placeImageXY(frwdSlashWall, (int) (s + (HEX_SIDE * 0.75) + 3 * j * HEX_SIDE),
                (int) (s + (a / 2) + (i / 2) * 2 * a));
          }
          else {
            ws.placeImageXY(frwdSlashWall, (int) (s + (HEX_SIDE * 0.75) + (1.5 + 3 * j) * HEX_SIDE),
                (int) (s + (a / 2) + ((i - 1) / 2 + 0.5) * 2 * a));
          }
        }
        if (v.topRight.equals(v)) {
          if (i % 2 == 0) {
            ws.placeImageXY(backSlashWall, (int) (s + (HEX_SIDE * 0.75) + 3 * j * HEX_SIDE),
                (int) (s - (a / 2) + (i / 2) * 2 * a));
          }
          else {
            ws.placeImageXY(backSlashWall, (int) (s + (HEX_SIDE * 0.75) + (1.5 + 3 * j) * HEX_SIDE),
                (int) (s - (a / 2) + ((i - 1) / 2 + 0.5) * 2 * a));
          }
        }
      }
    }

    if (this.player1.isFinished() || this.player2.isFinished()) {
      if (player1.isFinished() && !player2.isFinished()) {
        this.winner = "Player 1 Won!";
      }
      else if (player2.isFinished() && !player1.isFinished()) {
        this.winner = "Player 2 Won!";
      }

      TextImage gmOver = new TextImage("Game Over! " + winner, 24, Color.RED);
      TextImage wrngMoves1 = new TextImage("Player 1 had " + player1.wrongMoves + " wrong moves.",
          24, Color.RED);
      TextImage wrngMoves2 = new TextImage("Player 2 had " + player2.wrongMoves + " wrong moves.",
          24, Color.RED);
      WorldImage totImg = new AboveImage(gmOver, wrngMoves1, wrngMoves2);

      ws.placeImageXY(totImg, ws.width / 2, ws.height / 3);
    }
  }

  // onKeyEvent checks for keyboard input
  @Override
  public void onKeyEvent(String ke) {
    // menu choices
    if (this.menuChoice == 0) {
      if (ke.equals("1")) {
        this.menuChoice = 1;
        this.makeMaze();
        this.depthSearch();
      }
      else if (ke.equals("2")) {
        this.menuChoice = 2;
        this.makeMaze();
        this.breadthSearch();
      }
      else if (ke.equals("3")) {
        this.menuChoice = 3;
        this.makeMaze();
        this.depthSearch();
      }
      else if (ke.equals("4")) {
        this.menuChoice = 4;
        this.makeMaze();
        this.depthSearch();
        this.setEndVertices();
      }
    }

    // move player
    if (this.menuChoice == 3) {
      this.player.move(ke);
      if (this.player.equals(this.vertices.get(ROWS - 1).get(COLUMNS - 1))) {
        this.player.finish();
      }
    }

    // move players
    if (this.menuChoice == 4) {
      if (ke.equals("p")) {
        this.player2.move("w");
      }
      else if (ke.equals("[")) {
        this.player2.move("e");
      }
      else if (ke.equals("'")) {
        this.player2.move("d");
      }
      else if (ke.equals("/")) {
        this.player2.move("x");
      }
      else if (ke.equals(".")) {
        this.player2.move("z");
      }
      else if (ke.equals("l")) {
        this.player2.move("a");
      }
      else {
        this.player1.move(ke);
      }

      if (this.player1.equals(this.endVertex1)) {
        this.player1.finish();
      }
      if (this.player2.equals(this.endVertex2)) {
        this.player2.finish();
      }
    }

    // reset option
    if (ke.equals("r")) {
      this.reset();
    }
  }

  // onTick
  public void onTick() {
    // help animate DFS and BFS
    if (this.menuChoice > 0 && this.menuChoice < 3) {
      this.worldTicks++;
    }
  }
}
