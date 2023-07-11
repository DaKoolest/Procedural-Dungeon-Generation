package procedural_dungeon_gen;

public class Main {
	
	public static void main(String[] args){
		GeneratedMap map = new GeneratedMap(30, 30);
		map.generateMap();
		map.drawInConsole();

		map.displayMapImage(7, 7, 4);
	}  
}
