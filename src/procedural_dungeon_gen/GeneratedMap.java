package procedural_dungeon_gen;

import java.awt.Point;
import java.util.Random;

/**
 * Represents a generated map in the form of a grid where each unit is
 * designated as a cell, with Rooms occupying certain cells and not others. The
 * generated maps are procedurally generated and are randomly created (assuming
 * the same seed is not used). To store data, the map is represented by a 2D
 * Array of the enum Cells, with values representing different values of the
 * cells. Then, using this grid, the program populates the map with Rooms,
 * storing them in a list in the GeneratedMap class. The Rooms store their
 * relationships with the rooms around them, which makes the generated map
 * traversable. All rooms generated in the map will be reachable (no island room
 * cells surrounded by empty cells). When created, a generated map will be
 * initially unpopulated. To generate a map, call the generateMap() method.
 * 
 * @author Zach Dakoulas
 */
public class GeneratedMap {

	// represents a cell in the generated map
	private enum Cell { UNPROCESSED, PROCESSED, EMPTY, ROOM }

	private final int mapWidth, mapHeight;
	private Cell mapGrid[][];

	// the percent of the map that should be empty
	private final float percentEmpty;

	// max width and height of empty spaces and rooms that can be generated
	private final int maxEmptyWidth, maxEmptyHeight, maxRoomWidth, maxRoomHeight;

	private Random rand = new Random();
	/**
	 * Creates a new, unprocessed generated map.
	 * 
	 * @param mapWidth  Width of the generated map
	 * @param mapHeight Height of the generated map
	 */
	public GeneratedMap(int mapWidth, int mapHeight) {
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;

		percentEmpty = 0.35f;
		maxEmptyWidth = 2;
		maxEmptyHeight = 2;
		maxRoomWidth = 4;
		maxRoomHeight = 3;

		this.mapGrid = new Cell[mapHeight][mapWidth];
	}

	/**
	 * Creates a new, unprocessed generated map.
	 * 
	 * @param mapWidth     Width of the generated map
	 * @param mapHeight    Height of the generated map
	 * @param percentEmpty percentEmpty Percent of map that should be empty cells
	 * @param maxEmptyWidth Max width of empty cell rectangle
	 * @param maxEmptyHeight Max height of empty cell rectangle
	 */
	public GeneratedMap(int mapWidth, int mapHeight, float percentEmpty, int maxEmptyWidth, int maxEmptyHeight,
		int maxRoomWidth, int maxRoomHeight) {

		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;

		this.percentEmpty = percentEmpty;
		this.maxEmptyWidth = maxEmptyWidth;
		this.maxEmptyHeight = maxEmptyHeight;

		this.maxRoomWidth = maxRoomWidth;
		this.maxRoomHeight = maxRoomHeight;

		this.mapGrid = new Cell[mapHeight][mapWidth];
	}

	/**
	 * Generates a new layout of rooms and empty cells
	 */
	public void generateMap() {
		// populates map with empty cells
		createEmptyCells();

		// populates non-empty cells with rooms
		createRooms();
	}

	/**
	 * Populates non-empty cells with rooms of varying sizes
	 */
	private void createRooms() {

		// randomly chooses dimensions for the first placed room, looping until the dimensions are valid
		// and can be placed
		Point seedRoomTopLeft = new Point(), seedRoomBotRight = new Point();
		do {
			seedRoomTopLeft.x = rand.nextInt(mapWidth);
			seedRoomTopLeft.y = rand.nextInt(mapHeight);

			seedRoomBotRight.x = seedRoomTopLeft.x + rand.nextInt(maxRoomWidth);
			seedRoomBotRight.y = seedRoomTopLeft.y + rand.nextInt(maxRoomHeight);

		} while (!canPlaceRoom(seedRoomTopLeft, seedRoomBotRight));

		Room seedRoom = placeRoom(seedRoomTopLeft, seedRoomBotRight);
	}

	/**
	 * Places a room at the given position, creating a new Room object and updating the cells in mapGrid
	 * 
	 * @param topLeft Top left corner of the rectangle to be placed
	 * @param botRight Bottom right corner of the rectangle to be placed
	 * @return A new room that was placed at the given position
	 */
	private Room placeRoom(Point topLeft, Point botRight) {
		fillGrid(topLeft, botRight, Cell.ROOM);
		return new Room(topLeft, botRight);
	}

	/**
	 * Sets a percentage of cells on the grid to empty such that all non-empty cells
	 * are connected and reachable
	 */
	private void createEmptyCells() {
		// number of empty cells the grid should contain
		int numEmptyCells = (int) (mapWidth * mapHeight * percentEmpty);

		// loops until a map is generated such that all cells can be reached
		do {
			// sets all cells in grid to UNPROCESSED
			fillGrid(new Point(0, 0), new Point(mapWidth - 1, mapHeight - 1), Cell.UNPROCESSED);

			int emptyCellCount = 0;

			// loops until enough empty cells have been created
			while (emptyCellCount < numEmptyCells) {
				int width = rand.nextInt(maxEmptyWidth);
				int height = rand.nextInt(maxEmptyHeight);

				int xPos = rand.nextInt(mapWidth);
				int yPos = rand.nextInt(mapHeight);

				// position of the corners of the empty rectangle of cells
				Point topLeft = new Point(xPos, yPos);
				Point botRight = new Point(xPos + width, yPos + height);

				if (canPlaceRoom(topLeft, botRight)) {
					// sets the rectangle of cells to empty if it fits on the grid
					fillGrid(topLeft, botRight, Cell.EMPTY);

					// adds the area of the empty rectangle to the empty cell count
					emptyCellCount += (width + 1) * (height + 1);
				}
			}

		} while (!allCellsReachable());
	}

	/**
	 * Checks if all non-empty cells can be reached by only traversing up, down,
	 * left, or right between non-empty cells (ie all non-empty cells are connected)
	 * 
	 * @return True if all non-empty cells are reachable, false if not
	 */
	private boolean allCellsReachable() {
		int totalUnprocessedCells = 0;
		Point startPnt = new Point();

		// iterates through mapGrid, counting all unprocessed cells (which will become
		// room cells)
		for (int y = 0; y < mapHeight; y++) {
			for (int x = 0; x < mapWidth; x++) {
				if (mapGrid[y][x].equals(Cell.UNPROCESSED)) {
					// sets startPnt to the cords of the unprocessed cell
					startPnt.x = x;
					startPnt.y = y;

					totalUnprocessedCells++;
				}
			}
		}

		if (numCellsReachable(startPnt.x, startPnt.y) == totalUnprocessedCells) {
			// if the number of cells reachable from startPnt is equal to the total unprocessed
			// cells, return true
			return true;
		}

		return false;
	}

	/**
	 * Recursive helper method to allCellsReachable(), that returns the number of
	 * reachable cells from a starting point at position (x,y) on the grid.
	 * Traverses through unprocessed cells, marking them as processed to prevent an
	 * infinite loop of checking the same few cells indefinitely.
	 * 
	 * @param x The x position of the cell being processed
	 * @param y The y position of the cell being processed
	 * 
	 * @return The number of cells reachable from the point (x,y), with a minimum of
	 *         0 if the cell at the position is not unprocessed or not in bounds
	 * 
	 */
	private int numCellsReachable(int x, int y) {
		int numReachable = 0;

		if (!(x < 0 || x >= mapWidth || y < 0 || y >= mapHeight)) {
			if (mapGrid[y][x].equals(Cell.UNPROCESSED)) {
				// if cell in bounds and unprocessed, increase count and set
				// cell to processed
				numReachable = 1;
				mapGrid[y][x] = Cell.PROCESSED;

				// adds the num of reachable cells from each of its neighbors to the count
				numReachable += numCellsReachable(x - 1, y);
				numReachable += numCellsReachable(x + 1, y);
				numReachable += numCellsReachable(x, y - 1);
				numReachable += numCellsReachable(x, y + 1);
			}
		}

		return numReachable;
	}

	/**
	 * Checks if a room/rectangle of cells can be placed without overlapping another room
	 * 
	 * @param topLeft Top left corner of the rectangle to be placed
	 * @param botRight Bottom right corner of the rectangle to be placed
	 * @return True if a room with corners at positions topLeft and bottomRight can
	 * be placed without overlapping other processed cells and is in bounds, false
	 * if not.
	 */
	private boolean canPlaceRoom(Point topLeft, Point botRight) {
		if (isRoomInBounds(topLeft, botRight)) {
			// if both corners of the room are in bounds
			for (int y = topLeft.y; y <= botRight.y; y++) {
				for (int x = topLeft.x; x <= botRight.x; x++) {
					Cell val = mapGrid[y][x];
					
					// checks that every cell in room to be placed does not contain and empty
					// or room cell
					if (!(val.equals(Cell.UNPROCESSED)) && !val.equals(Cell.PROCESSED))
						return false;
				}
			}

			return true;
		}

		return false;
	}

	/**
	 * Checks if a room would be in bounds if placed
	 * 
	 * @param topLeft Top left corner of the rectangle to be placed
	 * @param botRight Bottom right corner of the rectangle to be placed
	 * @return True if both corners are in bounds, false if not
	 */
	private boolean isRoomInBounds(Point topLeft, Point botRight) {
		if (topLeft.x < 0 || botRight.x < 0)
			return false;
		if (topLeft.x > mapWidth || botRight.x >= mapWidth)
			return false;
		if (topLeft.y < 0 || botRight.y < 0)
			return false;
		if (topLeft.y > mapHeight || botRight.y >= mapHeight)
			return false;

		return true;
	}

	/**
	 * Fills the cells in rectangle from topLeft to botRight on the grid with a
	 * specified Cell enum value.
	 * 
	 * @param topLeft Top left corner of the rectangle to be placed
	 * @param botRight Bottom right corner of the rectangle to be placed
	 * @param val The Cell enum value that the rectangle of cells should be set to
	 */
	private void fillGrid(Point topLeft, Point botRight, Cell val) {
		for (int y = topLeft.y; y <= botRight.y; y++) {
			for (int x = topLeft.x; x <= botRight.x; x++) {
				mapGrid[y][x] = val;
			}
		}
	}

	/**
	 * Draws map in console, where '~' represents an UNPROCESSED cell, 'O' represents a PROCESSED
	 * cell, 'X' represents an EMPTY cell, and 'R' represents a ROOM cell
	 */
	public void drawInConsole() {
		for (int y = 0; y < mapHeight; y++) {
			for (int x = 0; x < mapWidth; x++) {
				switch (mapGrid[y][x]) {

				case UNPROCESSED:
					System.out.print("~");
					break;
				case PROCESSED:
					System.out.print("O");
					break;
				case EMPTY:
					System.out.print("X");
					break;
				case ROOM:
					System.out.print("R");
					break;
				}
			}
			System.out.println();
		}
	}
}
