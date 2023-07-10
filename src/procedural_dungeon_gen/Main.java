package procedural_dungeon_gen;

public class Main {
	
	public static void main(String[] args){
		GeneratedMap map = new GeneratedMap(10, 10);
		map.generateMap();
		map.drawInConsole();

		map.displayMapImage(9, 7, 1, 8);
	}  
}
