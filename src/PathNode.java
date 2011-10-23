import java.util.ArrayList;
import java.util.List;


public class PathNode implements Comparable<PathNode> {
	PathNode parent;
	Tile tile;
	int totalCost;
	int steps;
	int distanceToGoal;
	
	public PathNode(PathNode parent, Tile tile, int steps, int distanceToGoal) {
		super();
		this.parent = parent;
		this.tile = tile;
		this.totalCost = steps + distanceToGoal;
		this.steps = steps;
		this.distanceToGoal = distanceToGoal;
	}
	
	@Override
	public int compareTo(PathNode o) {
		if (this.totalCost > o.totalCost) {
			return 1;
		}
		
		if (this.totalCost < o.totalCost) {
			return -1;
		}
		
		return 0;
	}

	public List<PathNode> getFullPath() {
		ArrayList<PathNode> path = new ArrayList<PathNode>();
		if (parent != null) {
			path.addAll(parent.getFullPath());
		}
		path.add(this);
		return path;
	}
	
	public Tile getFirstTileInPath() {
		PathNode first = this.parent;
		PathNode second = this;
		while (first != null && first.parent != null) {
			second = first;
			first = first.parent;
		}
		return second.tile;
	}
}
