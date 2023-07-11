package procedural_dungeon_gen;

public class Main {
	
	public static void main(String[] args){
		GeneratedMap map = new GeneratedMap(20, 15);
		map.generateMap();
		//map.drawInConsole();

		map.displayMapImage(9, 9, 4);
	}  
}
