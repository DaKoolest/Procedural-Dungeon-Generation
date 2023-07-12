package procedural_dungeon_gen;

public class Main {
	
	public static void main(String[] args){
		GeneratedMap map = new GeneratedMap(30, 20, 0.32f,
			3, 3, 3, 2);
		map.generateMap();
		//map.drawInConsole();

		map.displayMapImage(5, 5, 6);
	}  
}
