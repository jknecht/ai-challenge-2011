import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {
	
	
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
    

    
    
    /**
     * For every ant check every direction in fixed order (N, E, S, W) and move it if the tile is
     * passable.
     */
    @Override
    public void doTurn() {
    	Ants ants = getAnts();
    	HashMap<Tile, Tile> orders = new HashMap<Tile, Tile>(50);
    	
    	
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
    		if (!orders.containsKey(distance.ant) && !targetedFood.contains(distance.destination) && doMoveLocation(ants, orders, distance.ant, distance.destination, "food")) {
    			targetedFood.add(distance.destination);
    		}
    	}

    	// find enemy hills
    	ArrayList<AntDistance> distancesFromHills = new ArrayList<AntDistance>();
    	HashSet<Tile> targetedHills = new HashSet<Tile>();
    	
    	for (Tile hill : ants.getEnemyAnts()) {
    		for (Tile ant : ants.getMyAnts()) {
    			distancesFromHills.add(new AntDistance(ants.getDistance(ant, hill), ant, hill));
    		}
    	}
    	Collections.sort(distancesFromHills);
    	//System.out.println("Hill identified: " + distancesFromHills.size());
    	for (AntDistance distance : distancesFromHills) {
    		if (!orders.containsKey(distance.ant) && doMoveLocation(ants, orders, distance.ant, distance.destination, "enemy hill")) {
    			//System.out.println("HILL TARGETED: " + distance.destination + " by ant " + distance.ant);
    			targetedHills.add(distance.destination);
    		}
    	}

    	
    	// find enemies
    	ArrayList<AntDistance> distancesFromEnemies = new ArrayList<AntDistance>(50);
    	HashSet<Tile> targetedEnemies = new HashSet<Tile>(50);
    	
    	for (Tile enemy : ants.getEnemyAnts()) {
    		for (Tile ant : ants.getMyAnts()) {
    			distancesFromEnemies.add(new AntDistance(ants.getDistance(ant, enemy), ant, enemy));
    		}
    	}
    	Collections.sort(distancesFromEnemies);
    	//System.out.println("Enemies identified: " + distancesFromEnemies.size());
    	for (AntDistance distance : distancesFromEnemies) {
    		if (!orders.containsKey(distance.ant)) {
    			if (doMoveLocation(ants, orders, distance.ant, distance.destination, "enemy")) {
//        			//System.out.println("ENEMY TARGETED: " + distance.destination + " by ant " + distance.ant);
        			targetedEnemies.add(distance.destination);
    			}
    		}
    	}
    
    	
    	
		List<Aim> aims = Arrays.asList(Aim.values());
        for (Tile myAnt : ants.getMyAnts()) {
        	if (!orders.containsValue(myAnt)) {
	            for (Aim direction : aims) {
	                if (doMoveDirection(ants, orders, myAnt, direction)) {
	        			//System.out.println("RANDOM MOVE: " + direction + " by ant " + myAnt);
	                    break;
	                }
	            }
        	}
        }
        
        

    }
    
    
    
    public boolean doMoveDirection(Ants ants, HashMap<Tile, Tile> orders, Tile antLoc, Aim direction) {
        // Track all moves, prevent collisions
        Tile newLoc = ants.getTile(antLoc, direction);
        if (!ants.getMyHills().contains(newLoc) && ants.getIlk(newLoc).isUnoccupied() && ants.getIlk(newLoc).isPassable() && !orders.containsValue(newLoc) && !orders.containsKey(antLoc)) {
            ants.issueOrder(antLoc, direction);
            orders.put(antLoc, newLoc);
            return true;
        } else {
            return false;
        }
    }

    public boolean doMoveLocation(Ants ants, HashMap<Tile, Tile> orders, Tile ant, Tile destination, String why) {
    	//System.out.println("Moving from " + ant + " to " + destination + " for " + why);
    	List<PathNode> open = new ArrayList<PathNode>();
    	HashMap<Tile, PathNode> openMap = new HashMap<Tile, PathNode>();
    	PathNode start = new PathNode(null, ant, 0, ants.getDistance(ant, destination));
    	open.add(start);
    	openMap.put(start.tile, start);
    	
    	HashMap<Tile, PathNode> closed = new HashMap<Tile, PathNode>();
    	
    	while(open.size() > 0) {
    		Collections.sort(open);
    		PathNode cheapest = open.get(0);
    		if (cheapest.tile.equals(destination)) {
    			//List<PathNode> path = cheapest.getFullPath();
    			//Tile firstTile = path.size() == 1 ? path.get(0).tile : path.get(1).tile;
    			Tile firstTile = cheapest.getFirstTileInPath();
    			//System.out.println("Getting directions from " + ant + " to " + firstTile);
    	    	List<Aim> directions = ants.getDirections(ant, firstTile);
    	    	for (Aim dir : directions) {
    	        	if (doMoveDirection(ants, orders, ant, dir)) {
    	        		return true;
    	        	}
    	    	}
    	    	return false;    			
    		}
    		
    		open.remove(0);
    		openMap.remove(cheapest.tile);
    		closed.put(cheapest.tile, cheapest);
    		List<Tile> neighbors = getNeighbors(cheapest.tile);
    		for (Tile neighbor : neighbors) {
    			PathNode neighborNode = closed.get(neighbor); 
    			boolean inClosed = (neighborNode != null);
	   			if (neighborNode != null && cheapest.steps < neighborNode.steps - 1) {
    				neighborNode.steps = cheapest.steps + 1;
    				neighborNode.parent = cheapest;
    			} else if (!inClosed) {
    				neighborNode = openMap.get(neighbor);
    	   			if (neighborNode != null && cheapest.steps < neighborNode.steps - 1) {
        				neighborNode.steps = cheapest.steps + 1;
        				neighborNode.parent = cheapest;
        			} else {
        				PathNode next = new PathNode(cheapest, neighbor, cheapest.steps + 1, ants.getDistance(neighbor, destination));
        				open.add(next);
        				openMap.put(next.tile, next);
        			}   				
    			}
    		}
    	}
    	return false;
    }

            		
    private PathNode find(List<PathNode> nodes, Tile tile) {
    	for (PathNode node : nodes) {
    		if (node.tile.equals(tile)) {
    			return node;
    		}
    	}
    	return null;
    }
    
    private List<Tile> getNeighbors(Tile tile) {
    	ArrayList<Tile> neighbors = new ArrayList<Tile>();
    	Ants ants = getAnts();
    	for (Aim aim : Aim.values()) {
    		Tile neighbor = ants.getTile(tile, aim);
    		if (ants.getIlk(neighbor).isPassable()) {
    			neighbors.add(neighbor);
    		}
    	}
    	return neighbors;
    }
    
    public boolean doMoveLocationSimple(Ants ants, HashMap<Tile, Tile> orders, Tile ant, Tile destination, String why) {
    	List<Aim> directions = ants.getDirections(ant, destination);
    	for (Aim dir : directions) {
        	if (doMoveDirection(ants, orders, ant, dir)) {
        		return true;
        	}
    	}
    	return false;
    }

    
}
