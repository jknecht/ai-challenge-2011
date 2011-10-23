public class AntDistance implements Comparable<AntDistance> {
	int distance;
	Tile ant;
	Tile destination;
	public AntDistance(int distance, Tile ant, Tile destination) {
		super();
		this.distance = distance;
		this.ant = ant;
		this.destination = destination;
	}
	
	@Override
	public String toString() {
		return "Ant: " + ant + ", Food: " + destination + ", Distance: " + distance;
	}
	@Override
	public int compareTo(AntDistance o) {
		if (this.distance > o.distance) {
			return 1;
		}
		if (this.distance < o.distance) {
			return -1;
		}
		return 0;
	}
	
	
}