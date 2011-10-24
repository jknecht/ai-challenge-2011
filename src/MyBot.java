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
    	Set<Tile> antTiles = ants.getMyAnts();
    	
    	//clear the dead ants
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
    	for (Tile antTile : antTiles) {
    		Ant ant = myAnts.get(antTile);
    		if (ant == null) {
    			ant = new Ant(ants, antTile);
    			myAnts.put(antTile, ant);
    		}
    		antsWithoutOrders.add(ant);
    	}
    	
    	
    	HashMap<Tile, Tile> orders = new HashMap<Tile, Tile>(myAnts.size());
    	
    	
    	knownEnemyHills.addAll(ants.getEnemyHills());
    	Set<Tile> occupiedHills = new HashSet<Tile>();
    	for (Tile hill : knownEnemyHills) {
    		Ant occupyingForce = myAnts.get(hill);
    		occupiedHills.add(hill);
    		if (occupyingForce != null) {
    			antsWithoutOrders.remove(occupyingForce);
    		}
    	}
    	
    	
    	
    	// find food
    	for (Tile food : ants.getFoodTiles()) {
    		Collections.sort(antsWithoutOrders, Ant.distanceComparator(ants, food));
    		for (Ant closest : antsWithoutOrders) {
    			closest.setDestination(food);
	    		if (closest.move(orders)) {
	    			antsWithoutOrders.remove(closest);
	    			break;
	    		}
    		}
    	}

    	
    	// find enemy hills
    	HashSet<Ant> hillAttackers = new HashSet<Ant>();
    	for (Tile hill : knownEnemyHills) {
    		if (occupiedHills.contains(hill)) {
    			continue;
    		}
    		Collections.sort(antsWithoutOrders, Ant.distanceComparator(ants, hill));
    		for (int i = 0; i < antsWithoutOrders.size() / knownEnemyHills.size(); i++) {
    			Ant closest = antsWithoutOrders.get(i);
    			closest.setDestination(hill);
        		if (closest.move(orders)) {
        			hillAttackers.add(closest);
	    		}
    		}
    	}
    	antsWithoutOrders.removeAll(hillAttackers);

    	
    	// find enemies
    	for (Tile enemy : ants.getEnemyAnts()) {
    		Collections.sort(antsWithoutOrders, Ant.distanceComparator(ants, enemy));
    		for (Ant closest : antsWithoutOrders) {
    			closest.setDestination(enemy);
        		if (closest.move(orders)) {
	    			antsWithoutOrders.remove(closest);
	    			break;
	    		}
    		}
    	}
    
    	
    	// if there was a destination set, then continue toward that destination
    	HashSet<Ant> onAPath = new HashSet<Ant>();
        for (Ant ant : antsWithoutOrders) {
        	if (ant.destination != null && ant.move(orders)) {
        		onAPath.add(ant);
        	}
        }
        antsWithoutOrders.removeAll(onAPath);
    	
    	
        for (Ant ant : antsWithoutOrders) {
        	ant.moveInPreferredDirection(orders);
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
    
            		
    
}
