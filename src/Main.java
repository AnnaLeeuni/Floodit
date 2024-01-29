import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

import java.util.Random;

//Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;

  Color color;
  boolean flooded;
  // the four adjacent cells to this one

  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;

    this.left = null;
    this.right = null;
    this.top = null;
    this.bottom = null;
  }

}

// class FloodItWorld
class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<Cell> board;

  // state the size of the board
  int boardsize;

  // the number of the colors
  int colorNumber;

  Random random;

  Random randTester;

  // for part 2
  int steps;
  int maxSteps;
  boolean gameOver;

  ArrayList<Cell> floodedList;

  // first constructor
  FloodItWorld(int boardsize, int colorNumber) {
    this.boardsize = boardsize;
    this.colorNumber = colorNumber;

    this.random = new Random();

    this.board = makeBoard(boardsize, colorNumber);


    this.steps = 0;
    this.maxSteps = (boardsize * boardsize * colorNumber) / 4 ;
    this.gameOver = false;

    this.floodedList = new ArrayList<>();
    this.floodedList.add(board.get(0));

    connectCells();
  }

  // constructor for testing
  FloodItWorld(int boardsize, int colorNumber, Random random) {
    this.boardsize = boardsize;
    this.colorNumber = colorNumber;
    this.random = random;

    this.board = makeBoard(boardsize, colorNumber);


    this.steps = 0;
    this.maxSteps = (boardsize * boardsize * colorNumber) / 4;
    this.gameOver = false;

    this.floodedList = new ArrayList<>();
    this.floodedList.add(board.get(0));

    connectCells();
  }

  // method makeBoard that create the x, y and color of each cell
  ArrayList<Cell> makeBoard(int boardsize, int colorNumber) {

    ArrayList<Cell> board = new ArrayList<>();

    ArrayList<Color> colorList = new ArrayList<>(Arrays.asList(Color.GREEN, Color.BLUE, Color.RED,
            Color.YELLOW, Color.PINK, Color.CYAN, Color.GRAY, Color.ORANGE));

    // colorNumber max is 8

    for (int y = 0; y < boardsize; y++) {
      for (int x = 0; x < boardsize; x++) {
        int colorIndex = this.random.nextInt(colorNumber);
        board.add(new Cell(x, y, colorList.get(colorIndex), false));
      }
    }
    return board;
  }

  // create a background which size
  // depends on the boardSize
  WorldImage background = new RectangleImage(boardsize * 20, boardsize * 20, OutlineMode.SOLID,
          Color.BLACK);

  // method makeScene that render the cell on the board
  public WorldScene makeScene() {

    onTick();

    WorldScene scene = this.getEmptyScene();
    scene.placeImageXY(background, boardsize, boardsize);

    for (Cell cell : board) {
      WorldImage cellImage = new RectangleImage(20, 20, "solid", cell.color);
      scene.placeImageXY(cellImage, cell.x * 20 + 10, cell.y * 20 + 10);
    }

    if (gameOver) {
      scene.placeImageXY(new TextImage("Game Over", 32, FontStyle.BOLD, Color.RED), boardsize * 10,
              boardsize * 10);
    }

    return scene;

  }

  // Effect :Connects all adjacent cells
  void connectCells() {
    for (Cell cell : board) {
      int x = cell.x;
      int y = cell.y;

      int currentIndex = y * boardsize + x;

      if (x > 0) {
        cell.left = board.get(currentIndex - 1);
      }
      if (x < boardsize - 1) {
        cell.right = board.get(currentIndex + 1);
      }
      if (y > 0) {
        cell.top = board.get((y - 1) * boardsize + x);
      }
      if (y < boardsize - 1) {
        cell.bottom = board.get((y + 1) * boardsize + x);
      }
    }
  }

  // Implement onKeyEvent method
  // Effect: reload the world when pressing r
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.steps = 0;
      this.gameOver = false;

      this.board = makeBoard(boardsize, colorNumber);

      this.floodedList.clear();
      this.floodedList.add(board.get(0));
      connectCells();
    }
  }



  // Implement checkWin method
  boolean checkWin() {
    Color firstColor = board.get(0).color;
    for (Cell cell : board) {
      if (!cell.color.equals(firstColor)) {
        return false;
      }
    }
    return true;
  }

  // get the specific cell by the posn
  Cell getCell(int x, int y) {
    if (x < 0 || x >= boardsize || y < 0 || y >= boardsize) {
      return null;
    }
    return board.get(y * boardsize + x);
  }

  // Effect: gets the position clicked from the computer
  public void onMouseClicked(Posn pos) {

    int x = pos.x / 20;
    int y = pos.y / 20;

    if (!gameOver) {

      steps++;

      for (Cell cell : board) {

        if (x == cell.x && y == cell.y) {
          // check color
          // if same color as the currentColor
          // no change
          if (cell.color.equals(this.board.get(0).color)) {
            return;
          }
          else {
            // or change first color to this color

            board.get(0).color = cell.color;

            for (Cell floodedCell : floodedList) {
              floodedCell.color = cell.color;
            }

          }

          if (steps >= maxSteps) {
            gameOver = true;
            // return end Scene:
          }

          if (floodedList.size() == boardsize * boardsize) {
            gameOver = true;
          }

        }

      }
      onTick();
    }

  }

  // a helper in onTick
  //Effect: add the right neighbor to the new to flood list
  void addList(List<Cell> floodedList, List<Cell> newToFlood, Cell neighbor) {
    if (neighbor != null && !floodedList.contains(neighbor)
            && neighbor.color.equals(floodedList.get(0).color)) {
      newToFlood.add(neighbor);
    }
  }

  // the method OnTick check all the time if
  // there are cell to be flooded
  //Effect: update the flooded list
  public void onTick() {
    // System.out.println(currentColor);
    if (!gameOver) {
      ArrayList<Cell> newToFlood = new ArrayList<>();

      for (Cell cell : floodedList) {
        addList(floodedList, newToFlood, cell.left);
        addList(floodedList, newToFlood, cell.top);
        addList(floodedList, newToFlood, cell.right);
        addList(floodedList, newToFlood, cell.bottom);
      }

      for (Cell toBeFloodedCell : newToFlood) {
        toBeFloodedCell.color = board.get(0).color;
        floodedList.add(toBeFloodedCell);
      }

      if (checkWin()) {
        gameOver = true;
        // return the endScene
      }
    }
  }

  // method to check worldEnd, either game won or lose
  public WorldEnd worldEnds() {
    if (gameOver) {
      WorldScene endScene = this.makeScene();
      return new WorldEnd(true, endScene);
    }
    return new WorldEnd(false, this.makeScene());
  }
}

// examples and tests
class Examples {

  // run the game
  void testFloodItWorld(Tester t) {

    // change the parameter and see the game:
    FloodItWorld officialWorld = new FloodItWorld(7, 5);// the range of colorNumber:1 - 8

    // for testing
    FloodItWorld starterWorld = new FloodItWorld(8, 7, new Random(888888888));

    // official world:
    // officialWorld.bigBang(500, 500, 1);
    // System.out.println(officialWorld.currentColor);

    // testing world:
    starterWorld.bigBang(500, 500, 2);

  }

  Random random;
  // cell Image according to color
  WorldImage cellImageGreen;
  WorldImage cellImageBlue;
  // for testing world1
  FloodItWorld world1;
  ArrayList<Cell> expectedBoard1;
  WorldScene worldScene1;
  WorldImage background1;
  // for testing world2
  FloodItWorld world2;
  ArrayList<Cell> expectedBoard2;
  WorldScene worldScene2;
  WorldImage background2;
  // for connect testing
  Cell cellOne;
  Cell cellUpdated;
  // test addList
  ArrayList<Cell> intFlood;
  ArrayList<Cell> intNewFlood;
  Cell nonEmpty;
  Cell empty;
  // for onKeyEvent
  FloodItWorld onKeyExam;
  FloodItWorld test;
  // test add cell
  Cell addCell;
  Posn pos;

  // set the initial condition
  void initWorld() {

    random = new Random();
    cellImageGreen = new RectangleImage(20, 20, "solid", Color.GREEN);
    cellImageBlue = new RectangleImage(20, 20, "solid", Color.BLUE);

    // for testing world1
    world1 = new FloodItWorld(1, 1, new Random(888888888));
    expectedBoard1 = new ArrayList<Cell>();
    expectedBoard1.add(new Cell(0, 0, Color.GREEN, false)); // one green cell at (0, 0)

    // for testing world1
    background1 = new RectangleImage(1 * 20, 1 * 20, OutlineMode.SOLID, Color.BLACK);

    worldScene1 = world1.getEmptyScene();
    worldScene1.placeImageXY(background1, 1, 1);
    cellImageGreen = new RectangleImage(20, 20, "solid", Color.GREEN);
    worldScene1.placeImageXY(cellImageGreen, 10, 10);

    // for testing world2
    world2 = new FloodItWorld(2, 2, new Random(888888888));
    expectedBoard2 = new ArrayList<Cell>();
    expectedBoard2.add(new Cell(0, 0, Color.GREEN, false));
    expectedBoard2.add(new Cell(1, 0, Color.BLUE, false));
    expectedBoard2.add(new Cell(0, 1, Color.BLUE, false));
    expectedBoard2.add(new Cell(1, 1, Color.GREEN, false));
    // should looks like:
    // green blue
    // blue green

    // for testinng world2
    background2 = new RectangleImage(2 * 20, 2 * 20, OutlineMode.SOLID, Color.BLACK);

    worldScene2 = world2.getEmptyScene();
    worldScene2.placeImageXY(background2, 2, 2);
    worldScene2.placeImageXY(cellImageGreen, 10, 10);
    worldScene2.placeImageXY(cellImageBlue, 30, 10);
    worldScene2.placeImageXY(cellImageBlue, 10, 30);
    worldScene2.placeImageXY(cellImageBlue, 30, 30);

    // add cell
    addCell = new Cell(50, 50, Color.black, true);

  }

  // test makeBoard
  void testMakeBoard(Tester t) {
    initWorld();

    // test to see if world1 is a single green cell
    t.checkExpect(this.world1.makeBoard(1, 1), expectedBoard1);
    // test on world2
    t.checkExpect(this.world2.makeBoard(2, 2), expectedBoard2);
    // test when colorNumber is 0
    t.checkException(new IllegalArgumentException("bound must be positive"), this.world1,
            "makeBoard", 1, 0);
    // test when colorNumber is larger than 8
    t.checkException(new IndexOutOfBoundsException("Index 9 out of bounds for length 8"),
            this.world1, "makeBoard", 1, 10);

  }

  // test makeScene
  void testMakeScene(Tester t) {
    initWorld();
    t.checkExpect(this.world1.makeScene(), worldScene1);
    t.checkExpect(this.world2.makeScene(), worldScene2);
  }


  // test onkeyEvent
  void testOnKeyEvent(Tester t) {
    initWorld();

    FloodItWorld worldForOnKeyEvent = new FloodItWorld(5, 3, new Random(123));
    int initialBoardSize = worldForOnKeyEvent.board.size();

    worldForOnKeyEvent.gameOver = true;
    worldForOnKeyEvent.steps = 5;

    worldForOnKeyEvent.onKeyEvent("r");

    t.checkExpect(worldForOnKeyEvent.gameOver, false, "Test if gameOver is set to false");
    t.checkExpect(worldForOnKeyEvent.steps, 0, "Test if steps are reset to 0");
    t.checkExpect(worldForOnKeyEvent.board.size(), initialBoardSize,
            "Test if the board is recreated");
  }


  // tests add list
  void testAddList(Tester t) {
    FloodItWorld world = new FloodItWorld(3, 3);

    ArrayList<Cell> floodedList = new ArrayList<>();
    ArrayList<Cell> newToFlood = new ArrayList<>();
    Cell neighborToAdd = world.getCell(1, 0);

    world.board.get(0).color = Color.GREEN;
    neighborToAdd.color = Color.GREEN;

    floodedList.add(world.getCell(0, 0));

    world.addList(floodedList, newToFlood, neighborToAdd);

    t.checkExpect(newToFlood.size(), 1, "Failed to add neighbor to newToFlood");
    t.checkExpect(newToFlood.get(0), neighborToAdd, "Incorrect neighbor added to newToFlood");
  }


  // test getCell
  void testGetCell(Tester t) {
    initWorld();

    FloodItWorld worldForGetCell = new FloodItWorld(3, 2, new Random(456));
    ArrayList<Cell> boardForGetCell = new ArrayList<>(Arrays.asList(
            new Cell(0, 0, Color.GREEN, false),
            new Cell(1, 0, Color.BLUE, false),
            new Cell(2, 0, Color.GREEN, false),
            new Cell(0, 1, Color.BLUE, false),
            new Cell(1, 1, Color.GREEN, false),
            new Cell(2, 1, Color.BLUE, false),
            new Cell(0, 2, Color.GREEN, false),
            new Cell(1, 2, Color.BLUE, false),
            new Cell(2, 2, Color.GREEN, false)
    ));
    worldForGetCell.board = boardForGetCell;

    t.checkExpect(worldForGetCell.getCell(0, 0), boardForGetCell.get(0), "Test getCell(0, 0)");
    t.checkExpect(worldForGetCell.getCell(1, 0), boardForGetCell.get(1), "Test getCell(1, 0)");
    t.checkExpect(worldForGetCell.getCell(2, 1), boardForGetCell.get(5), "Test getCell(2, 1)");

    t.checkExpect(worldForGetCell.getCell(-1, 0), null, "Test getCell for out of bounds (-1, 0)");
    t.checkExpect(worldForGetCell.getCell(0, -1), null, "Test getCell for out of bounds (0, -1)");
    t.checkExpect(worldForGetCell.getCell(3, 0), null, "Test getCell for out of bounds (3, 0)");
    t.checkExpect(worldForGetCell.getCell(0, 3), null, "Test getCell for out of bounds (0, 3)");
  }



  // tests connect cells
  void testConnectCells(Tester t) {
    FloodItWorld testWorld = new FloodItWorld(3, 3, new Random(888888888));

    // Check if adjacent cells are properly connected
    Cell topLeft = testWorld.getCell(0, 0);
    t.checkExpect(topLeft.left, null);
    t.checkExpect(topLeft.top, null);
    t.checkExpect(topLeft.right, testWorld.getCell(1, 0));
    t.checkExpect(topLeft.bottom, testWorld.getCell(0, 1));

    Cell middle = testWorld.getCell(1, 1);
    t.checkExpect(middle.left, testWorld.getCell(0, 1));
    t.checkExpect(middle.top, testWorld.getCell(1, 0));
    t.checkExpect(middle.right, testWorld.getCell(2, 1));
    t.checkExpect(middle.bottom, testWorld.getCell(1, 2));
  }


  // test the method onTick
  public void testOnTick() {
    // Prepare the test world with the actual initial state
    FloodItWorld testWorld = new FloodItWorld(2, 2);
    testWorld.board.get(0).color = Color.GREEN;
    testWorld.board.get(1).color = Color.RED;
    testWorld.board.get(2).color = Color.RED;
    testWorld.board.get(3).color = Color.GREEN;

    // Call onTick once
    testWorld.onTick();

    // Check if the board has the expected state after onTick
    Tester t = new Tester();
    t.checkExpect(testWorld.board.get(0).color, Color.GREEN);
    t.checkExpect(testWorld.board.get(1).color, Color.GREEN);
    t.checkExpect(testWorld.board.get(2).color, Color.RED);
    t.checkExpect(testWorld.board.get(3).color, Color.GREEN);
  }

  // test onMouseClicked
  void testOnMouseClicked(Tester t) {
    initWorld();
    FloodItWorld testWorld = new FloodItWorld(3, 3, new Random(888888888));
    // Test clicking on a cell with a different color
    testWorld.onMouseClicked(new Posn(40, 40));
    t.checkExpect(testWorld.steps, 1);
    // Test clicking on a cell with the same color
    testWorld.onMouseClicked(new Posn(0, 0));
    t.checkExpect(testWorld.steps, 2);
    // Test clicking outside the board
    testWorld.onMouseClicked(new Posn(1000, 1000));
    t.checkExpect(testWorld.steps, 3);
    // Test clicking when the game is over
    testWorld.gameOver = true;
    testWorld.onMouseClicked(new Posn(40, 40));
    t.checkExpect(testWorld.steps, 3);
    initWorld();
    FloodItWorld testerWorld = new FloodItWorld(3, 3, new Random(888888888));
    testerWorld.gameOver = false;
    // Ensure that the game is not over to test the onMouseClicked method
    Color initialColor = testerWorld.board.get(0).color;
    // Click on a cell and check if the color of the first cell has changed
    testerWorld.onMouseClicked(new Posn(30, 10));
    t.checkOneOf(testerWorld.board.get(0).color, initialColor);
    // The color of the first cell should not be the same
    // Check if the floodedList is updated correctly
    testerWorld.onMouseClicked(new Posn(30, 10));
    t.checkExpect(testerWorld.floodedList.size(), 1);
    // The floodedList should have one more element after the click
  }

  // Test checkWin method
  void testCheckWin(Tester t) {
    initWorld();
    // Test case 1: Winning board
    FloodItWorld winningBoard = new FloodItWorld(2, 2, new Random(123456789));
    winningBoard.board.get(0).color = Color.RED;
    winningBoard.board.get(1).color = Color.RED;
    winningBoard.board.get(2).color = Color.RED;
    winningBoard.board.get(3).color = Color.RED;
    t.checkExpect(winningBoard.checkWin(), true);
    // Test case 2: Non-winning board
    FloodItWorld nonWinningBoard = new FloodItWorld(2, 2, new Random(987654321));
    nonWinningBoard.board.get(0).color = Color.RED;
    nonWinningBoard.board.get(1).color = Color.BLUE;
    nonWinningBoard.board.get(2).color = Color.GREEN;
    nonWinningBoard.board.get(3).color = Color.YELLOW;
    t.checkExpect(nonWinningBoard.checkWin(), false);
    // Test case 3: Another non-winning board
    FloodItWorld nonWinningBoard2 = new FloodItWorld(3, 3, new Random(555555555));
    nonWinningBoard2.board.get(0).color = Color.RED;
    nonWinningBoard2.board.get(1).color = Color.RED;
    nonWinningBoard2.board.get(2).color = Color.RED;
    nonWinningBoard2.board.get(3).color = Color.RED;
    nonWinningBoard2.board.get(4).color = Color.RED;
    nonWinningBoard2.board.get(5).color = Color.RED;
    nonWinningBoard2.board.get(6).color = Color.RED;
    nonWinningBoard2.board.get(7).color = Color.RED;
    nonWinningBoard2.board.get(8).color = Color.BLUE;
    t.checkExpect(nonWinningBoard2.checkWin(), false);

  }

  //test worldEnds
  void testWorldEnds(Tester t) {
    initWorld();
    FloodItWorld worldForWorldEnds1 = new FloodItWorld(5, 3, new Random(123));
    FloodItWorld worldForWorldEnds2 = new FloodItWorld(5, 3, new Random(123));

    worldForWorldEnds1.gameOver = true;

    WorldEnd expectedWorldEnd1 = new WorldEnd(true, worldForWorldEnds1.makeScene());
    WorldEnd expectedWorldEnd2 = new WorldEnd(false, worldForWorldEnds2.makeScene());

    t.checkExpect(worldForWorldEnds1.worldEnds(), expectedWorldEnd1, "Test if the game is over");
    t.checkExpect(worldForWorldEnds2.worldEnds(), expectedWorldEnd2, "Test if the game continues");
  }



}
