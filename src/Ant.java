import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class Ant {
	static final Random r = new Random(System.currentTimeMillis());
	Ants ants;
	Tile tile;
	Tile destination;
	Tile order;
	Aim preferredDirection;
	Aim currentDirection;
	int turnCount = 0;
	int turnBias = 0;
	int moveCount = 0;
	
	public Ant(Ants ants, Tile tile) {
		super();
		this.ants = ants;
		this.tile = tile;
		int randomDirection = r.nextInt(Aim.values().length);
		preferredDirection = Aim.values()[randomDirection];
		turnBias = r.nextInt(2);
	}
	
	public void setDestination(Tile destination) {
		this.destination = destination;
	}
	
	public static Comparator<Ant> distanceComparator(final Ants ants, final Tile target) {
		
		return new Comparator<Ant>() {

			@Override
			public int compare(Ant o1, Ant o2) {
				int d1 = ants.getDistance(o1.tile, target);
				int d2 = ants.getDistance(o2.tile, target);
				if (d1 > d2) return 1;
				if (d2 > d1) return -1;
				return 0;
			}
		};
		
	}
	
	public boolean moveInPreferredDirection(HashMap<Tile, Tile> orders) {
		//System.err.println("Tile " + tile + ", Prefer " + preferredDirection + ", current " + currentDirection + ", turns " + turnCount);
		this.destination = null;
		if (currentDirection == null) {
			currentDirection = preferredDirection;
		}
		if (currentDirection.equals(preferredDirection) && turnCount == 0) {
			if (move(orders, currentDirection)) {
				return true;
			} else {
				for (int i = 0; i < 3; i++) {
					if (turnBias == 0) {
						currentDirection = currentDirection.rightTurn();
					} else {
						currentDirection = currentDirection.leftTurn();
					}
					turnCount++;
					if (move(orders, currentDirection)) {
						return true;
					}
				}
			}
		} else {
			//try to turn left to get back to our preferred direction
			if (turnBias == 0) {
				currentDirection = currentDirection.leftTurn();
			} else {
				currentDirection = currentDirection.rightTurn();
			}
			turnCount--;
			if (move(orders, currentDirection)) {
				return true;
			} else {
				for (int i = 0; i < 3; i++) {
					if (turnBias == 0) {
						currentDirection = currentDirection.rightTurn();
					} else {
						currentDirection = currentDirection.leftTurn();
					}
					turnCount++;
					if (move(orders, currentDirection)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
    public boolean move(HashMap<Tile, Tile> orders, Aim direction) {
        // Track all moves, prevent collisions
    	Tile newLoc = ants.getTile(this.tile, direction);
    	//System.err.println("Attempting to move tile " + this.tile);
        if ((ants.getMyAnts().size() == 1 || !ants.getMyHills().contains(newLoc)) && ants.getIlk(newLoc).isUnoccupied() && ants.getIlk(newLoc).isPassable() && !orders.containsValue(newLoc) && !orders.containsKey(this.tile)) {
            ants.issueOrder(this.tile, direction);
            orders.put(this.tile, newLoc);
            this.order = newLoc;
            return true;
        } else {
            return false;
        }
    }

    public boolean move(HashMap<Tile, Tile> orders) {
    	//System.err.println("Moving " + tile + " toward " + destination);
    	List<PathNode> open = new ArrayList<PathNode>();
    	HashMap<Tile, PathNode> openMap = new HashMap<Tile, PathNode>();
    	PathNode start = new PathNode(null, tile, 0, ants.getDistance(tile, destination));
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
    	    	List<Aim> directions = ants.getDirections(this.tile, firstTile);
    	    	for (Aim dir : directions) {
    	        	if (move(orders, dir)) {
    	            	return true;
    	        	}
    	    	}
    	    	this.destination = null;
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
        			} else if (neighborNode == null){
        				PathNode next = new PathNode(cheapest, neighbor, cheapest.steps + 1, ants.getDistance(neighbor, destination));
        				open.add(next);
        				openMap.put(next.tile, next);
        			}   				
    			}
    		}
    	}
    	this.destination = null;
    	return false;
    }

    private List<Tile> getNeighbors(Tile tile) {
    	ArrayList<Tile> neighbors = new ArrayList<Tile>();
    	for (Aim aim : Aim.values()) {
    		Tile neighbor = ants.getTile(tile, aim);
    		if (ants.getIlk(neighbor).isPassable()) {
    			neighbors.add(neighbor);
    		}
    	}
    	return neighbors;
    }

}
