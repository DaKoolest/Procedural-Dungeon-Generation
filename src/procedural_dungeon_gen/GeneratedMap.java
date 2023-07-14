package procedural_dungeon_gen;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.swing.*;
import procedural_dungeon_gen.Room.Direction;
import java.awt.*;
import java.awt.image.BufferedImage;

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
	private enum Cell {
		UNPROCESSED, PROCESSED, EMPTY, ROOM
	}

	private int mapWidth, mapHeight;
	private Cell mapGrid[][];
	private Room mapRoomGrid[][];
	private ArrayList<Room> rooms = new ArrayList<>();

	// the percent of the map that should be empty
	private float percentEmpty;

	// max width and height of empty spaces and rooms that can be generated
	private int maxEmptyWidth, maxEmptyHeight, maxRoomWidth, maxRoomHeight;

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

		percentEmpty = 0.25f;
		maxEmptyWidth = 3;
		maxEmptyHeight = 2;
		maxRoomWidth = 3;
		maxRoomHeight = 3;

		this.mapGrid = new Cell[mapHeight][mapWidth];
		this.mapRoomGrid = new Room[mapHeight][mapWidth];
	}

	/**
	 * Creates a new, unprocessed generated map.
	 * 
	 * @param mapWidth       Width of the generated map
	 * @param mapHeight      Height of the generated map
	 * @param percentEmpty   percentEmpty Percent of map that should be empty cells
	 * @param maxEmptyWidth  Max width of empty cell rectangle
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
		this.mapRoomGrid = new Room[mapHeight][mapWidth];
	}

	/**
	 * Generates a new layout of rooms and empty cells
	 */
	public void generateMap() {
		rooms.clear();
		this.mapRoomGrid = new Room[mapHeight][mapWidth];

		System.out.println("Generating empty cells");
		// populates map with empty cells
		createEmptyCells();
		System.out.println("Finished generating empty cells");

		System.out.println("Generating rooms");
		// populates non-empty cells with rooms
		createRooms();
		System.out.println("Finished generating rooms");

		Room room1, room2;
		do {
			room1 = rooms.get(rand.nextInt(rooms.size()));
		 	room2 = rooms.get(rand.nextInt(rooms.size()));
			
		} while (room1 == room2 || room1.getArea() != 1 || room2.getArea() != 1 || findShortestDistance(room1, room2) != 5);

		System.out.println("Room 1 area:" + room1.getArea() + ", Room 2 area:" + room2.getArea());
		room1.setColor(Color.GREEN);
		room2.setColor(Color.GREEN);
	}

	/**
	 * Populates non-empty cells with rooms of varying sizes
	 */
	private void createRooms() {

		// randomly chooses dimensions for the first placed room, looping until the
		// dimensions are valid and can be placed
		Point seedRoomTopLeft = new Point(), seedRoomBotRight = new Point();
		do {
			seedRoomTopLeft.x = rand.nextInt(mapWidth);
			seedRoomTopLeft.y = rand.nextInt(mapHeight);
			seedRoomBotRight.x = seedRoomTopLeft.x + rand.nextInt(maxRoomWidth);
			seedRoomBotRight.y = seedRoomTopLeft.y + rand.nextInt(maxRoomHeight);

		} while (!canPlaceRoom(seedRoomTopLeft, seedRoomBotRight));

		// places 'seed' room (colored blue)
		Room seedRoom = placeRoom(seedRoomTopLeft, seedRoomBotRight, Color.BLUE);
		rooms.add(seedRoom);

		// recursively generates the rest of rooms
		placeRoomsAround(seedRoom);

		// iterates through rooms and randomly adds doorways to make dungeon less linear
		for (Room room: rooms) {
			
			// 20% chance to generate an extra doorway on a room
			if (rand.nextFloat() < 0.3f) { 
				Point roomTopLeft = room.getTopLeftPos();
				Point roomBotRight = room.getBotRightPos();
				Iterator<Point> points = getAdjacentCells(roomTopLeft, roomBotRight).iterator();
				boolean exitLoop = false;
			
				// iterates through adjacent rooms that room is not connected
				while (points.hasNext() && !exitLoop) {
					Point point = points.next();
					
					Room otherRoom = mapRoomGrid[point.y][point.x];

					if (otherRoom != null && !room.isConnectedToRoom(otherRoom)) {
						if (point.x < roomTopLeft.x) { // left
							otherRoom.addConnectedRoom(room, point, Direction.RIGHT);
							room.addConnectedRoom(otherRoom, new Point(point.x + 1, point.y), Direction.LEFT);

						} else if (point.x > roomBotRight.x) { // right
							otherRoom.addConnectedRoom(room, point, Direction.LEFT);
							room.addConnectedRoom(otherRoom, new Point(point.x - 1, point.y), Direction.RIGHT);

						} else if (point.y < roomTopLeft.y) { // above
							otherRoom.addConnectedRoom(room, point, Direction.DOWN);
							room.addConnectedRoom(otherRoom, new Point(point.x, point.y + 1), Direction.UP);

						} else { // below
							otherRoom.addConnectedRoom(room, point, Direction.UP);
							room.addConnectedRoom(otherRoom, new Point(point.x, point.y - 1), Direction.DOWN);
						}
						exitLoop = true;
					}
				}
				
			}
		}

	}

	/**
	 * This is a recursive method that populates the map with random rooms. It does this by
	 * checking for non-occupied cells around the dimensions of the room, and randomly choosing
	 * dimensions on the rooms created, and then calling itself on those said rooms.
	 * 
	 * @param room Room that other rooms should be placed around
	 */
	private void placeRoomsAround(Room room) {
		Point topLeft = room.getTopLeftPos();
		Point botRight = room.getBotRightPos();

		for (Point adjPoint: getAdjacentCells(room.getTopLeftPos(), room.getBotRightPos())) {

			if (adjPoint.x < topLeft.x) { // handles generating rooms to the left
				if (mapGrid[adjPoint.y][topLeft.x - 1].equals(Cell.PROCESSED)) {
					
					Point newTopLeft = new Point(), newBotRight = new Point();
					// if cell is processed -- meaning it's not empty or part of a room,
					// create random dimensions for a room until one fits
					do {
						
						int newRoomWidth = rand.nextInt(maxRoomWidth) + 1;
						
						int newRoomHeight = rand.nextInt(maxRoomHeight) + 1;
						// horizontal offset that the top left corner of the new room should have
						// compared to the "entrance" cell from the current room
						int verticalOffset = rand.nextInt(newRoomHeight);

						newTopLeft.x = topLeft.x - newRoomWidth;
						newTopLeft.y = adjPoint.y - verticalOffset;

						newBotRight.x = topLeft.x - 1;
						newBotRight.y = newTopLeft.y + newRoomHeight - 1;

					} while (!canPlaceRoom(newTopLeft, newBotRight));

					Room newRoom = placeRoom(newTopLeft, newBotRight, Color.RED);

					// adds connections so that the rooms can be traversed
					newRoom.addConnectedRoom(room, new Point(adjPoint.x, adjPoint.y), Direction.RIGHT);
					room.addConnectedRoom(newRoom, new Point(adjPoint.x + 1, adjPoint.y), Direction.LEFT);

					rooms.add(newRoom);

					// recursively places rooms around the new room
					placeRoomsAround(newRoom);
				}

			} else if (adjPoint.x > botRight.x) { // handles generating rooms to the right
				if (mapGrid[adjPoint.y][botRight.x + 1].equals(Cell.PROCESSED)) {
					
					Point newTopLeft = new Point(), newBotRight = new Point();
					// if cell is processed -- meaning it's not empty or part of a room,
					// create random dimensions for a room until one fits
					do {
						
						int newRoomWidth = rand.nextInt(maxRoomWidth) + 1;
						int newRoomHeight = rand.nextInt(maxRoomHeight) + 1;

						// horizontal offset that the top left corner of the new room should have
						// compared to the "entrance" cell from the current room
						int verticalOffset = rand.nextInt(newRoomHeight);

						newTopLeft.x = botRight.x + 1;
						newTopLeft.y = adjPoint.y - verticalOffset;

						newBotRight.x = botRight.x + newRoomWidth;
						newBotRight.y = newTopLeft.y + newRoomHeight - 1;

					} while (!canPlaceRoom(newTopLeft, newBotRight));

					Room newRoom = placeRoom(newTopLeft, newBotRight, Color.RED);

					// adds connections so that the rooms can be traversed
					newRoom.addConnectedRoom(room, new Point(adjPoint.x, adjPoint.y), Direction.LEFT);
					room.addConnectedRoom(newRoom, new Point(adjPoint.x - 1, adjPoint.y), Direction.RIGHT);

					rooms.add(newRoom);

					// recursively places rooms around the new room
					placeRoomsAround(newRoom);
				}

			} else if (adjPoint.y < topLeft.y) { // handles generating rooms above
				if (mapGrid[topLeft.y - 1][adjPoint.x].equals(Cell.PROCESSED)) {
					
					Point newTopLeft = new Point(), newBotRight = new Point();
					// if cell is processed -- meaning it's not empty or part of a room,
					// create random dimensions for a room until one fits
					do {
						
						int newRoomWidth = rand.nextInt(maxRoomWidth) + 1;
						// horizontal offset that the top left corner of the new room should have
						// compared to the "entrance" cell from the current room
						int horizontalOffset = rand.nextInt(newRoomWidth);
						
						int newRoomHeight = rand.nextInt(maxRoomHeight) + 1;

						newTopLeft.x = adjPoint.x - horizontalOffset;
						newTopLeft.y = topLeft.y - newRoomHeight;

						newBotRight.x = newTopLeft.x + newRoomWidth - 1;
						newBotRight.y = topLeft.y - 1;

					} while (!canPlaceRoom(newTopLeft, newBotRight));

					Room newRoom = placeRoom(newTopLeft, newBotRight, Color.RED);

					// adds connections so that the rooms can be traversed
					newRoom.addConnectedRoom(room, new Point(adjPoint.x,  adjPoint.y + 1), Direction.DOWN);
					room.addConnectedRoom(newRoom, new Point(adjPoint.x,  adjPoint.y), Direction.UP);

					rooms.add(newRoom);

					// recursively places rooms around the new room
					placeRoomsAround(newRoom);
				}
			} else { // handles generting rooms below
				if (mapGrid[botRight.y + 1][adjPoint.x].equals(Cell.PROCESSED)) {
					
					Point newTopLeft = new Point(), newBotRight = new Point();
					// if cell is processed -- meaning it's not empty or part of a room,
					// create random dimensions for a room until one fits
					do {
						
						int newRoomWidth = rand.nextInt(maxRoomWidth) + 1;
						// horizontal offset that the top left corner of the new room should have
						// compared to the "entrance" cell from the current room
						int horizontalOffset = rand.nextInt(newRoomWidth);
						
						int newRoomHeight = rand.nextInt(maxRoomHeight) + 1;

						newTopLeft.x = adjPoint.x - horizontalOffset;
						newTopLeft.y = botRight.y + 1;

						newBotRight.x = newTopLeft.x + newRoomWidth - 1;
						newBotRight.y = botRight.y + newRoomHeight;

					} while (!canPlaceRoom(newTopLeft, newBotRight));

					Room newRoom = placeRoom(newTopLeft, newBotRight, Color.RED);

					// adds connections so that the rooms can be traversed
					newRoom.addConnectedRoom(room, new Point(adjPoint.x,  adjPoint.y - 1), Direction.UP);
					room.addConnectedRoom(newRoom, new Point(adjPoint.x,  adjPoint.y), Direction.DOWN);

					rooms.add(newRoom);

					// recursively places rooms around the new room
					placeRoomsAround(newRoom);
				}
			}
		}
	}

	/**
	 * Gets the adjacent cells around a room in a random order
	 * @param topLeft Top left position of room
	 * @param botRight Bot right position of room
	 * @return An ArrayList of Points that hold the coordinates to the adjacent cells of a given room
	 */
	private ArrayList<Point> getAdjacentCells(Point topLeft, Point botRight) {
		ArrayList<Point> adjCells = new ArrayList<>();

		if (topLeft.y > 0) {
			for (int x = topLeft.x; x <= botRight.x; x++) {
				adjCells.add(new Point(x, topLeft.y - 1));
			}
		}

		if (botRight.y < mapHeight - 1) {
			for (int x = topLeft.x; x <= botRight.x; x++) {
				adjCells.add(new Point(x, botRight.y + 1));
			}
		}

		if (topLeft.x > 0) {
			for (int y = topLeft.y; y <= botRight.y; y++) {
				adjCells.add(new Point(topLeft.x - 1, y));
			}
		}

		if (botRight.x < mapWidth - 1) {
			for (int y = topLeft.y; y <= botRight.y; y++) {
				adjCells.add(new Point(botRight.x + 1, y));
			}
		}

		Collections.shuffle(adjCells, rand);
		return adjCells;
	}

	/**
	 * Places a room at the given position, creating a new Room object and updating
	 * the cells in mapGrid
	 * 
	 * @param topLeft  Top left corner of the rectangle to be placed
	 * @param botRight Bottom right corner of the rectangle to be placed
	 * @param color Color of room
	 * @return A new room that was placed at the given position
	 */
	private Room placeRoom(Point topLeft, Point botRight, Color color) {
		fillGrid(topLeft, botRight, Cell.ROOM);
		Room room = new Room(topLeft, botRight, color);

		for (int y = topLeft.y; y <= botRight.y; y++) {
			for (int x = topLeft.x; x <= botRight.x; x++) {
				mapRoomGrid[y][x] = room;
			}
		}
	
		fillGrid(topLeft, botRight, Cell.ROOM);
		return room;
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
			// if the number of cells reachable from startPnt is equal to the total
			// unprocessed
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
	 * Checks if a room/rectangle of cells can be placed without overlapping another
	 * room
	 * 
	 * @param topLeft  Top left corner of the rectangle to be placed
	 * @param botRight Bottom right corner of the rectangle to be placed
	 * @return True if a room with corners at positions topLeft and bottomRight can
	 *         be placed without overlapping other processed cells and is in bounds,
	 *         false
	 *         if not.
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
	 * @param topLeft  Top left corner of the rectangle to be placed
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
	 * @param topLeft  Top left corner of the rectangle to be placed
	 * @param botRight Bottom right corner of the rectangle to be placed
	 * @param val      The Cell enum value that the rectangle of cells should be set
	 *                 to
	 */
	private void fillGrid(Point topLeft, Point botRight, Cell val) {
		for (int y = topLeft.y; y <= botRight.y; y++) {
			for (int x = topLeft.x; x <= botRight.x; x++) {
				mapGrid[y][x] = val;
			}
		}
	}

	/**
     * Finds the shortest distance between two rooms using BFS.
     *
     * @param startRoom The starting room
     * @param targetRoom The target room
     * @return The shortest distance between the start and target rooms, or -1 if no path exists
     */
    public int findShortestDistance(Room startRoom, Room targetRoom) {
        // map to store the visited status of each room
        Map<Room, Boolean> visited = new HashMap<>();
        for (Room room : rooms) {
            visited.put(room, false);
        }

        // queue for BFS traversal
        Queue<Room> queue = new LinkedList<>();
        // map to store the distance from the start room to each room
        Map<Room, Integer> distance = new HashMap<>();

        // initialize the starting room
        visited.put(startRoom, true);
        distance.put(startRoom, 0);
        queue.add(startRoom);

        while (!queue.isEmpty()) {
            Room currentRoom = queue.poll();

            if (currentRoom == targetRoom) {
                // found the target room, return the distance
                return distance.get(currentRoom);
            }

            // iterate through connected rooms
            for (Room.Doorway doorway : currentRoom.getDoors()) {
                Room adjacentRoom = doorway.getConnectingRoom();

                if (!visited.get(adjacentRoom)) {
                    // mark the adjacent room as visited
                    visited.put(adjacentRoom, true);
                    // update the distance
                    distance.put(adjacentRoom, distance.get(currentRoom) + 1);
                    // add the adjacent room to the queue for further traversal
                    queue.add(adjacentRoom);
                }
            }
        }

        // no path exists between the start and target rooms
        return -1;
    }

	/**
	 * Draws map in console, where '~' represents an UNPROCESSED cell, 'O'
	 * represents a PROCESSED
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

	/**
	 * Displays the map using colored rectangles to represent, opening a new window.
	 * Cells are represented as 2D tiles on a grid.
	 * 
	 * @param tileWidth   Width of the tile in pixels
	 * @param tileHeight  Height of the tile in pixels
	 * @param pixelSize   Scale of the pixel size
	 */
	public void displayMapImage(int tileWidth, int tileHeight, int pixelSize) {

		tileWidth *= pixelSize;
		tileHeight *= pixelSize;

		int imageWidth = tileWidth * mapWidth;
		int imageHeight = tileHeight * mapHeight;

		// image that will be displayed
		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();

		// sets background of image to white
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, imageWidth, imageHeight);

		// iterates through each room drawing them as a rectangle from the top left to bottom right of the tile
		for (Room room : rooms) {
			Point topLeft = room.getTopLeftPos();
			Point botRight = room.getBotRightPos();

			int roomWidth = botRight.x - topLeft.x + 1;
			int roomHeight = botRight.y - topLeft.y + 1;

			graphics.setColor(room.getColor());

			graphics.fillRect(topLeft.x * tileWidth + pixelSize, topLeft.y * tileHeight + pixelSize,
					roomWidth * tileWidth - 2 * pixelSize, roomHeight * tileHeight - 2 * pixelSize);
			
			// draws doorways
			graphics.setColor(Color.MAGENTA);
			
			for (Room.Doorway door: room.getDoors()) {
				Point entrance = door.getEntrance();
				switch (door.directionFacing()) {
				
				case UP:
					graphics.fillRect(entrance.x * tileWidth + tileWidth/2 - pixelSize/2,
							topLeft.y * tileHeight, pixelSize, pixelSize);
					break;
					
				case DOWN:
					graphics.fillRect(entrance.x * tileWidth + tileWidth/2 - pixelSize/2,
							(topLeft.y + roomHeight) * tileHeight - pixelSize, pixelSize, pixelSize);
					break;
					
				case LEFT:
					graphics.fillRect(topLeft.x * tileWidth,
							entrance.y * tileHeight + tileHeight/2 - pixelSize/2, pixelSize, pixelSize);
					break;
				
				case RIGHT:
					graphics.fillRect((botRight.x + 1) * tileWidth - pixelSize,
							entrance.y * tileHeight + tileHeight/2 - pixelSize/2, pixelSize, pixelSize);
					break;
				
				}	
			}
		}

		graphics.dispose();

		// creates window to be displayed
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(imageWidth, imageHeight);
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));

		frame.pack();

		frame.setVisible(true);
	}
}
