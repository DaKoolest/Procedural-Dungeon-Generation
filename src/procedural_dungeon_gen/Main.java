package procedural_dungeon_gen;

public class Main {
	
	public static void main(String[] args){
		GeneratedMap gen_map = new GeneratedMap(10, 5);
		gen_map.generateMap();
		gen_map.drawInConsole();
	}  
}
