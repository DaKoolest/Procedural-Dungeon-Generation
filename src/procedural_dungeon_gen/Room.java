package procedural_dungeon_gen;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

/**
 * Represents a Room in a generated map.
 * 
 * @author Zach Dakoulas
 */
public class Room {
	private Point topLeftPos;
	private Point botRightPos;
	private Color color;

	// stores connecting rooms as a pair, where the Room is the connected room,
	// and point is the cell from the current room where the "doorway" is attached
	private ArrayList<Doorway> connectedRooms = new ArrayList<>();

	public static enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT
	}
	
	/**
	 * Creates a new room object
	 * 
	 * @param topLeftPos Top left corner of room
	 * @param botRightPos Bottom right corner of room
	 * @param color Color of room
	 */
	public Room(Point topLeftPos, Point botRightPos, Color color) {
		this.topLeftPos = topLeftPos;
		this.botRightPos = botRightPos; 
		this.color = color;
	}
	
	public Point getTopLeftPos() {
		return topLeftPos;
	}
	
	public Point getBotRightPos() {
		return botRightPos;
	}

	public int getArea() {
		return (botRightPos.x - topLeftPos.x + 1) * (botRightPos.y - topLeftPos.y + 1);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * 
	 * 
	 * @param otherRoom Room the calling room is connected to
	 * @param entrancePoint Position of the cell that contains the door
	 * @param dir Direction the door in the current room should be facing to point to otherRoom
	 */
	public void addConnectedRoom(Room otherRoom, Point entrancePoint, Direction dir) {
		connectedRooms.add(new Doorway(otherRoom, entrancePoint, dir));
	}

	public boolean isConnectedToRoom(Room otherRoom) {

		for (Doorway door: connectedRooms) {
			if (door.connectingRoom == otherRoom) {
				return true;
			}
		}

		return false;
	}
	
	public ArrayList<Doorway> getDoors() {
		return connectedRooms;
	}
	
	/**
	 * Acts as a container class that holds information about room connections, including the connecting room,
	 * entrance point, and direction the door is facing.
	 */
	public class Doorway {

		private Room connectingRoom;
		private Point entrancePoint; // Position of the cell that contains the door
		// Direction the door in the current room should be facing to point to otherRoom
		private Direction dir;
		
		private Doorway (Room connectingRoom, Point entrancePoint, Direction dir) {
			this.connectingRoom = connectingRoom;
			this.entrancePoint = entrancePoint;
			this.dir = dir;
		}
		
		public Point getEntrance() {
			return entrancePoint;
		}
		
		public Direction directionFacing() {
			return dir;
		}

		public Room getConnectingRoom() {
			return connectingRoom;
		}
		
	}

}