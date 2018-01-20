import java.util.*;

import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Assignment 10
//Hughes William 
//whughes
//Jeevanantham Arun
//arunj98

// a class to represent a MazeWorld
class MazeWorld extends World {
  final static int BORDER_WIDTH = 4;
  final static int WALL_WIDTH = 2;
  final int COLUMNS;
  final int ROWS;
  final int CELL_SIZE;
  final int WORLD_WIDTH;
  final int WORLD_HEIGHT;

  HashMap<Posn, Edge> cameFromEdge;
  HashMap<Posn, Posn> representatives;
  ArrayList<Edge> edgesInTree;
  ArrayList<Edge> worklist;
  ArrayList<Vertex> path;
  ArrayList<ArrayList<Vertex>> vertices;
  Stack<Vertex> depthWorklist;
  Queue<Vertex> breadthWorklist;
  int menuChoice;
  int worldTicks;
  Player player;
  Player player1;
  Player player2;
  Vertex endVertex1;
  Vertex endVertex2;
  double corridorChoice;
  String winner;

  MazeWorld(int columns, int rows) {
    this.COLUMNS = columns;
    this.ROWS = rows;
    this.CELL_SIZE = Math.min(1600 / this.COLUMNS, 800 / this.ROWS);
    this.WORLD_WIDTH = 2 * BORDER_WIDTH + COLUMNS * (WALL_WIDTH + CELL_SIZE) - WALL_WIDTH;
    this.WORLD_HEIGHT = 2 * BORDER_WIDTH + ROWS * (WALL_WIDTH + CELL_SIZE) - WALL_WIDTH;
    this.reset();
  }

  // resets all fields to default
  public void reset() {
    this.cameFromEdge = new HashMap<Posn, Edge>();
    this.representatives = new HashMap<Posn, Posn>();
    this.edgesInTree = new ArrayList<Edge>();
    this.worklist = new ArrayList<Edge>();
    this.path = new ArrayList<Vertex>();
    this.vertices = new ArrayList<ArrayList<Vertex>>();
    this.depthWorklist = new Stack<Vertex>();
    this.breadthWorklist = new LinkedList<Vertex>();
    this.menuChoice = 0;
    this.worldTicks = 0;
    this.player = new Player(new Posn(-1, -1));
    this.player1 = new Player(new Posn(-1, -1));
    this.player2 = new Player(new Posn(-1, -1));
    this.corridorChoice = 1;
    this.winner = "";
  }

  // Constructs maze using kruskal's algorithm
  // EFFECT: this classes vertices field is updated
  public void makeMaze() {
    // create representatives and vertices
    for (int i = 0; i < this.ROWS; i++) {
      this.vertices.add(new ArrayList<Vertex>());

      for (int j = 0; j < this.COLUMNS; j++) {
        Posn p = new Posn(j, i);
        this.representatives.put(p, p);
        this.vertices.get(i).add(new Vertex(p));
      }
    }

    // create worklist
    for (int i = 0; i < this.ROWS; i++) {
      for (int j = 0; j < this.COLUMNS; j++) {
        if (i < this.ROWS - 1) {
          this.worklist.add(new Edge(this.representatives.get(new Posn(j, i)),
              this.representatives.get(new Posn(j, i + 1)),
              (int) (Math.random() * this.ROWS * this.COLUMNS * this.corridorChoice)));
        }

        if (j < this.COLUMNS - 1) {
          this.worklist.add(new Edge(this.representatives.get(new Posn(j, i)),
              this.representatives.get(new Posn(j + 1, i)),
              (int) (Math.random() * this.ROWS * this.COLUMNS)));
        }
      }
    }

    // Sort the worklist by weight
    Collections.sort(this.worklist, new CompareByWeight());

    // create edgesInTree
    while (this.edgesInTree.size() < (this.ROWS * this.COLUMNS - 1)) {
      Edge min = this.worklist.get(0);
      if (!find(this.representatives, min.posA).equals(find(this.representatives, min.posB))) {
        this.edgesInTree.add(min);
        union(this.representatives, find(this.representatives, min.posA),
            find(this.representatives, min.posB));
      }
      this.worklist.remove(0);
    }

    // set vertices' neighbors
    for (Edge e : this.edgesInTree) {
      Posn pA = e.posA;
      Posn pB = e.posB;
      Vertex vA = this.vertices.get(pA.y).get(pA.x);
      Vertex vB = this.vertices.get(pB.y).get(pB.x);

      if (pA.x == pB.x && pA.y == pB.y - 1) {
        vA.bottom = vB;
        vB.top = vA;
      }
      else {
        vA.right = vB;
        vB.left = vA;
      }
    }
    this.player.moveTo(this.vertices.get(0).get(0));
    this.player1.moveTo(this.vertices.get(0).get(0));
    this.player2.moveTo(this.vertices.get(ROWS - 1).get(COLUMNS - 1));
  }

  // finds the representative
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

    for (Vertex v : this.path) {
      if (v.onPath) {
        pathLength++;
      }
    }

    for (Vertex v : this.path) {
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
  }

  // Depth-First searches the maze for the solution to the maze
  // EFFECT: this classes path field is updated
  public void depthSearch() {
    this.depthWorklist.push(this.vertices.get(0).get(0));

    while (!this.depthWorklist.isEmpty()) {
      Vertex next = this.depthWorklist.peek();

      if (next.processed) {
        this.depthWorklist.pop();
      }
      else if (next.equals(this.vertices.get(this.ROWS - 1).get(this.COLUMNS - 1))) {
        next.processed = true;
        this.path.add(next);
        this.reconstruct(next);
        return;
      }
      else {
        next.processed = true;
        Vertex top = next.top;
        Vertex bottom = next.bottom;
        Vertex left = next.left;
        Vertex right = next.right;
        List<Vertex> temp = new LinkedList<Vertex>(Arrays.asList(top, bottom, left, right));

        for (Vertex v : temp) {
          if (!v.processed) {
            this.depthWorklist.push(v);
            this.cameFromEdge.put(v.pos, new Edge(next.pos, v.pos, 0));
          }
        }
        this.path.add(next);
      }
    }
  }

  // Breadth-first searches the maze for the solution to the maze
  // EFFECT: this classes path field is updated
  // Breadth-first search
  public void breadthSearch() {
    this.breadthWorklist.add(this.vertices.get(0).get(0));

    while (!this.breadthWorklist.isEmpty()) {
      Vertex next = this.breadthWorklist.peek();

      if (next.processed) {
        this.breadthWorklist.poll();
      }
      else if (next.equals(this.vertices.get(this.ROWS - 1).get(this.COLUMNS - 1))) {
        next.processed = true;
        this.path.add(next);
        this.reconstruct(next);
        return;
      }
      else {
        next.processed = true;
        Vertex top = next.top;
        Vertex bottom = next.bottom;
        Vertex left = next.left;
        Vertex right = next.right;
        List<Vertex> temp = new LinkedList<Vertex>(Arrays.asList(top, bottom, left, right));

        for (Vertex v : temp) {
          if (!v.processed) {
            this.breadthWorklist.add(v);
            this.cameFromEdge.put(v.pos, new Edge(next.pos, v.pos, 0));
          }
        }
        this.path.add(next);
      }
    }
  }

  // reconstructs the path for the maze
  // EFFECT: sets onPath to true for vertices on solution path
  public void reconstruct(Vertex next) {
    if (!next.equals(this.vertices.get(0).get(0))) {
      next.onPath = true;
      Edge toPrev = this.cameFromEdge.get(next.pos);
      Posn prevPos = toPrev.posA;
      this.reconstruct(this.vertices.get(prevPos.y).get(prevPos.x));
    }
  }

  // Create world scene
  @Override
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(WORLD_WIDTH, WORLD_HEIGHT);
    ws.placeImageXY(
        new RectangleImage(WORLD_WIDTH, WORLD_HEIGHT, OutlineMode.SOLID, Color.DARK_GRAY),
        ws.width / 2, ws.height / 2);
    ws.placeImageXY(new RectangleImage(WORLD_WIDTH - 2 * BORDER_WIDTH,
        WORLD_HEIGHT - 2 * BORDER_WIDTH, OutlineMode.SOLID, Color.LIGHT_GRAY), ws.width / 2,
        ws.height / 2);
    if (this.menuChoice != 0) {
      makeImage(ws);
    }
    else {
      int fontSize = 24;
      String corridor;
      if (this.corridorChoice > 1.1) {
        corridor = "Horizontal";
      }
      else if (this.corridorChoice < 0.9) {
        corridor = "Vertical'";
      }
      else {
        corridor = "Normal";
      }
      TextImage header = new TextImage("Menu", fontSize * 3 / 2, Color.RED);
      TextImage dpth = new TextImage("1. Depth Search", fontSize, Color.RED);
      TextImage brdth = new TextImage("2. Breadth Search", fontSize, Color.RED);
      TextImage plyr = new TextImage("3. Manual Search", fontSize, Color.RED);
      TextImage twoPlyr = new TextImage("4. Two Player Intertwined", fontSize, Color.RED);
      TextImage corridors = new TextImage("Corridor preference: " + corridor + " ('c' to change)",
          fontSize, Color.BLUE);
      TextImage reset = new TextImage("Press 'r' to Reset", fontSize * 4 / 5, Color.BLUE);
      WorldImage menuImg = new AboveImage(header, dpth, brdth, plyr, twoPlyr, corridors, reset);
      ws.placeImageXY(menuImg, ws.width / 2, ws.height / 2);
    }
    return ws;
  }

  // Create maze image
  public void makeImage(WorldScene ws) {
    int cW = BORDER_WIDTH - WALL_WIDTH / 2;
    int cL = BORDER_WIDTH + CELL_SIZE / 2;
    int wallLength = CELL_SIZE + WALL_WIDTH;

    // building blocks
    RectangleImage vertWall = new RectangleImage(WALL_WIDTH, wallLength, "Solid", Color.DARK_GRAY);
    RectangleImage hrznWall = new RectangleImage(wallLength, WALL_WIDTH, "Solid", Color.DARK_GRAY);
    RectangleImage start = new RectangleImage(wallLength, wallLength, "Solid",
        Color.GREEN.darker().darker());
    RectangleImage finish = new RectangleImage(wallLength, wallLength, "Solid",
        new Color(200, 0, 200));
    CircleImage playerImg = new CircleImage(CELL_SIZE / 3, "Solid", this.player.color());
    CircleImage player1Img = new CircleImage(CELL_SIZE / 3, "Solid", this.player1.color());
    CircleImage player2Img = new CircleImage(CELL_SIZE / 3, "Solid", this.player2.color());

    // animate search for the path
    for (int i = 0; i < this.worldTicks && i < this.path.size(); i++) {
      Vertex v = this.path.get(i);
      ws.placeImageXY(new RectangleImage(wallLength, wallLength, "Solid", v.color()),
          cL + v.pos.x * wallLength, cL + v.pos.y * wallLength);
    }

    // show solution when player reaches the end
    if (this.player.isFinished()) {
      for (Vertex v : this.path) {
        if (v.onPath) {
          ws.placeImageXY(new RectangleImage(wallLength, wallLength, "Solid", v.color()),
              cL + v.pos.x * wallLength, cL + v.pos.y * wallLength);
        }
      }
      TextImage wrngMoves = new TextImage("You won! You had " + player.wrongMoves + " wrong moves.",
          24, Color.RED);
      ws.placeImageXY(wrngMoves, ws.width / 2, ws.height / 2);
    }

    // not two player start/end cells
    if (this.menuChoice != 4) {
      // draw start and end cells
      ws.placeImageXY(start, cL, cL);
      ws.placeImageXY(finish, cL + (COLUMNS - 1) * wallLength, cL + (ROWS - 1) * wallLength);
    }

    // draw single player
    if (this.menuChoice == 3) {
      ws.placeImageXY(playerImg, cL + player.pos.x * wallLength, cL + player.pos.y * wallLength);
    }

    // two players
    if (this.menuChoice == 4) {
      // show solution when a player reaches the end
      if (this.player1.isFinished() || this.player2.isFinished()) {
        for (Vertex v : this.path) {
          if (v.onPath) {
            ws.placeImageXY(new RectangleImage(wallLength, wallLength, "Solid", v.color()),
                cL + v.pos.x * wallLength, cL + v.pos.y * wallLength);
          }
        }
      }

      // draw start and end cells
      ws.placeImageXY(start, cL, cL);
      ws.placeImageXY(start, cL + (COLUMNS - 1) * wallLength, cL + (ROWS - 1) * wallLength);
      ws.placeImageXY(finish, cL + endVertex1.pos.x * wallLength,
          cL + endVertex1.pos.y * wallLength);
      ws.placeImageXY(finish, cL + endVertex2.pos.x * wallLength,
          cL + endVertex2.pos.y * wallLength);

      ws.placeImageXY(player1Img, cL + player1.pos.x * wallLength, cL + player1.pos.y * wallLength);
      ws.placeImageXY(player2Img, cL + player2.pos.x * wallLength, cL + player2.pos.y * wallLength);

      int i = this.endVertex1.pos.y;
      int j = this.endVertex1.pos.x;
      if (this.endVertex1.top.equals(endVertex2)) {
        ws.placeImageXY(hrznWall, cL + j * wallLength, cW + i * wallLength);
      }
      else if (this.endVertex1.left.equals(endVertex2)) {
        ws.placeImageXY(vertWall, cW + j * wallLength, cL + i * wallLength);
      }
      else if (this.endVertex1.bottom.equals(endVertex2)) {
        ws.placeImageXY(hrznWall, cL + j * wallLength, cW + (i + 1) * wallLength);
      }
      else if (this.endVertex1.right.equals(endVertex2)) {
        ws.placeImageXY(vertWall, cW + (j + 1) * wallLength, cL + i * wallLength);
      }
    }

    // draw maze walls
    for (int i = 0; i < this.ROWS; i++) {
      for (int j = 0; j < this.COLUMNS; j++) {
        Vertex v = vertices.get(i).get(j);
        if (v.top.equals(v)) {
          ws.placeImageXY(hrznWall, cL + j * wallLength, cW + i * wallLength);
        }
        if (v.left.equals(v)) {
          ws.placeImageXY(vertWall, cW + j * wallLength, cL + i * wallLength);
        }
        if (v.bottom.equals(v)) {
          ws.placeImageXY(hrznWall, cL + j * wallLength, cW + (i + 1) * wallLength);
        }
        if (v.right.equals(v)) {
          ws.placeImageXY(vertWall, cW + (j + 1) * wallLength, cL + i * wallLength);
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

      ws.placeImageXY(totImg, ws.width / 2, ws.height / 2);
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
      else if (ke.equals("c")) {
        if (this.corridorChoice > 1.01) {
          this.corridorChoice = 0.5;
        }
        else if (this.corridorChoice < 0.99) {
          this.corridorChoice = 1;
        }
        else {
          this.corridorChoice = 2;
        }
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
      if (ke.equals("w")) {
        this.player1.move("up");
      }
      else if (ke.equals("a")) {
        this.player1.move("left");
      }
      else if (ke.equals("s")) {
        this.player1.move("down");
      }
      else if (ke.equals("d")) {
        this.player1.move("right");
      }
      else {
        this.player2.move(ke);
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