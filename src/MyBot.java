import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {
	ArrayList<Tile> unseen;
	
	
    /**
     * Main method executed by the game engine for starting the bot.
     * 
     * @param args command line arguments
     * 
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        new MyBot().readSystemInput();
    }
    
    @Override
    public void setup(int loadTime, int turnTime, int rows, int cols,
    		int turns, int viewRadius2, int attackRadius2, int spawnRadius2) {
    	super.setup(loadTime, turnTime, rows, cols, turns, viewRadius2, attackRadius2,
    			spawnRadius2);
    	
    	unseen = new ArrayList<Tile>();
    	for (int i = 0; i < getAnts().getRows(); i++) {
    		for (int j = 0; j < getAnts().getCols(); j++) {
    			unseen.add(new Tile(i, j));
    		}
    	}
    }
    
    /**
     * For every ant check every direction in fixed order (N, E, S, W) and move it if the tile is
     * passable.
     */
    @Override
    public void doTurn() {
    	Ants ants = getAnts();
    	HashMap<Tile, Tile> orders = new HashMap<Tile, Tile>();
    	
    	
    	// find food
    	ArrayList<AntDistance> distancesFromFood = new ArrayList<AntDistance>();
    	HashSet<Tile> targetedFood = new HashSet<Tile>();
    	
    	for (Tile food : ants.getFoodTiles()) {
    		for (Tile ant : ants.getMyAnts()) {
    			distancesFromFood.add(new AntDistance(ants.getDistance(ant, food), ant, food));
    		}
    	}
    	Collections.sort(distancesFromFood);
    	for (AntDistance distance : distancesFromFood) {
    		if (!targetedFood.contains(distance.destination) && doMoveLocation(ants, orders, distance.ant, distance.destination)) {
    			targetedFood.add(distance.destination);
    		}
    	}

    	
    	// find enemies
    	ArrayList<AntDistance> distancesFromEnemies = new ArrayList<AntDistance>();
    	HashSet<Tile> targetedEnemies = new HashSet<Tile>();
    	
    	for (Tile enemy : ants.getEnemyAnts()) {
    		for (Tile ant : ants.getMyAnts()) {
    			distancesFromEnemies.add(new AntDistance(ants.getDistance(ant, enemy), ant, enemy));
    		}
    	}
    	Collections.sort(distancesFromEnemies);
    	for (AntDistance distance : distancesFromEnemies) {
    		if (doMoveLocation(ants, orders, distance.ant, distance.destination)) {
    			targetedEnemies.add(distance.destination);
    		}
    	}
    
    	
    	
        for (Tile myAnt : ants.getMyAnts()) {
        	if (!orders.containsValue(myAnt)) {
        		List<Aim> aims = Arrays.asList(Aim.values());
        		Collections.shuffle(aims);
	            for (Aim direction : Aim.values()) {
	                if (doMoveDirection(ants, orders, myAnt, direction)) {
	                    break;
	                }
	            }
        	}
        }
        
        

    }
    
    
    
    public boolean doMoveDirection(Ants ants, HashMap<Tile, Tile> orders, Tile antLoc, Aim direction) {
        // Track all moves, prevent collisions
        Tile newLoc = ants.getTile(antLoc, direction);
        if (!ants.getMyHills().contains(newLoc) && ants.getIlk(newLoc).isUnoccupied() && !orders.containsValue(newLoc) && !orders.containsKey(antLoc)) {
            ants.issueOrder(antLoc, direction);
            orders.put(antLoc, newLoc);
            return true;
        } else {
            return false;
        }
    }

    public boolean doMoveLocation(Ants ants, HashMap<Tile, Tile> orders, Tile ant, Tile destination) {
    	List<Aim> directions = ants.getDirections(ant, destination);
    	for (Aim dir : directions) {
        	if (doMoveDirection(ants, orders, ant, dir)) {
        		return true;
        	}
    	}
    	return false;
    }
    
    
}
