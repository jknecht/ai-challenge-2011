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
	HashSet<Tile> knownEnemyHills = new HashSet<Tile>();
	HashMap<Tile, Ant> myAnts = new HashMap<Tile, Ant>();
	
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
    	
    	ArrayList<Ant> antsWithoutOrders = new ArrayList<Ant>();
    	log("getting my ants");
    	Set<Tile> antTiles = ants.getMyAnts();
    	
    	//clear the dead ants
    	log("clearing dead ants");
    	Set<Tile> deadAnts = new HashSet<Tile>();
    	for (Tile antTile : myAnts.keySet()) {
    		if (!antTiles.contains(antTile)) {
    			deadAnts.add(antTile);
    		}
    	}
    	for (Tile dead : deadAnts) {
    		myAnts.remove(dead);
    	}
    	
    	//add any new ants and populate antsWithoutOrders list
    	log("adding new ants and populating antsWithoutOrders");
    	for (Tile antTile : antTiles) {
    		Ant ant = myAnts.get(antTile);
    		if (ant == null) {
    			ant = new Ant(ants, antTile);
    			myAnts.put(antTile, ant);
    		}
    		antsWithoutOrders.add(ant);
    	}
    	
    	
    	HashMap<Tile, Tile> orders = new HashMap<Tile, Tile>(myAnts.size());
    	
    	// find food
    	log("hunting food");
    	for (Tile food : ants.getFoodTiles()) {
    		log("sorting antsWithoutOrders by straightline distance to " + food);
    		Collections.sort(antsWithoutOrders, Ant.distanceComparator(ants, food));
    		int n = antsWithoutOrders.size() >= 5 ? 5 : antsWithoutOrders.size();
    		log("picking up to 5 closest ants...");
    		ArrayList<Ant> closeAnts = new ArrayList<Ant>();
    		for (int i = 0; i < n; i++) {
    			Ant potential = antsWithoutOrders.get(i);
    			//only include this ant if it can actually see the food
    			if (ants.getDistance(potential.tile, food) <= ants.getViewRadius2()) {
    				closeAnts.add(potential);
    			}
    		}
    		log("sorting closest ants by path distance to " + food);
    		Collections.sort(closeAnts, Ant.pathComparator(ants, food));
    		log("iterating over closest ants...");
    		for (Ant closest : closeAnts) {
    			Tile closestFood = closest.closestFood();
    			if (closestFood != null && closestFood.equals(food)) {
    				log("sending ant " + closest + " after food at " + food);
	    			closest.setDestination(food);
		    		if (closest.move(orders)) {
		    			antsWithoutOrders.remove(closest);
		    			break;
		    		}
    			}
    		}
    	}

    	// find enemy hills
    	log("hunting enemy hills");
    	HashSet<Ant> hillAttackers = new HashSet<Ant>();
    	for (Tile hill : ants.getEnemyHills()) {
    		Collections.sort(antsWithoutOrders, Ant.distanceComparator(ants, hill));
    		for (int i = 0; i < antsWithoutOrders.size() / ants.getEnemyHills().size(); i++) {
    			Ant closest = antsWithoutOrders.get(i);
    			closest.setDestination(hill);
        		if (closest.move(orders)) {
        			hillAttackers.add(closest);
	    		}
    		}
    	}
    	antsWithoutOrders.removeAll(hillAttackers);

    	
    	
    	// find enemies
//    	for (Tile enemy : ants.getEnemyAnts()) {
//    		Collections.sort(antsWithoutOrders, Ant.distanceComparator(ants, enemy));
//    		for (Ant closest : antsWithoutOrders) {
//    			closest.setDestination(enemy);
//        		if (closest.move(orders)) {
//	    			antsWithoutOrders.remove(closest);
//	    			break;
//	    		}
//    		}
//    	}
    
    	
    	// if there was a destination set, then continue toward that destination
    	HashSet<Ant> onAPath = new HashSet<Ant>();
        for (Ant ant : antsWithoutOrders) {
        	if (ant.destination != null && ant.move(orders)) {
        		onAPath.add(ant);
        	}
        }
        antsWithoutOrders.removeAll(onAPath);
    	
    	
        for (Ant ant : antsWithoutOrders) {
        	//ant.moveInPreferredDirection(orders);
        	ant.moveToPreferredTile(orders);
        }
        
        //update the position of my ants
        HashMap<Tile, Ant> newPositions = new HashMap<Tile, Ant>();
        for (Tile key : myAnts.keySet()) {
        	Ant ant = myAnts.get(key);
        	if (ant.order != null) {
        		ant.tile = ant.order;
        		ant.order = null;
        	}
    		newPositions.put(ant.tile, ant);
        }
        myAnts = newPositions;
    }
    
            	
    private void log(String msg) {
    	if (Ant.debug)
    		System.err.println(msg);
    }
    
}
