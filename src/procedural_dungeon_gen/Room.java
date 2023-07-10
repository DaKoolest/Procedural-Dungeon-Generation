package procedural_dungeon_gen;
import java.awt.Color;
import java.awt.Point;

/**
 * Represents a Room in a generated map.
 * 
 * @author Zach Dakoulas
 */
public class Room {
	private Point topLeftPos;
	private Point botRightPos;
	private Color color;
	
	public Room(Point topLeftPos, Point botRightPos) {
		this.topLeftPos = topLeftPos;
		this.botRightPos = botRightPos; 
		color = Color.RED;
	}
	
	public Point getTopLeftPos() {
		return topLeftPos;
	}
	
	public Point getBotRightPos() {
		return botRightPos;
	}
	
	public Color getColor() {
		return color;
	}
}