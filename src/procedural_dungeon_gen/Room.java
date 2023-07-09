package procedural_dungeon_gen;
import java.awt.Point;

/**
 * Represents a Room in a generated map.
 * 
 * @author Zach Dakoulas
 */
public class Room {
	private Point topLeftPos;
	private Point botRightPos;
	
	public Room(Point topLeftPos, Point botRightPos) {
		this.topLeftPos = topLeftPos;
		this.botRightPos = botRightPos; 
	}
}
