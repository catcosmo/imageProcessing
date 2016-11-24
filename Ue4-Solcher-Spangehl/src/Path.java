import java.util.ArrayList;
import java.util.List;

public class Path {

	List<Pixel> pixels = new ArrayList<Pixel>();
	List<Edge> edges = new ArrayList<Edge>();
	List<Vertex> vertices = new ArrayList<Vertex>();
	int[] pivotList;
	List<Integer> possiblePolygonStartPoints = new ArrayList<Integer>();
	private boolean outer;
	int optimalStartPosition;

	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	public void addPixel(Pixel pixel) {
		pixels.add(pixel);

	}

	public List<Pixel> getPixels() {
		return pixels;
	}

	public void setPixels(List<Pixel> pixels) {
		this.pixels = pixels;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

	public boolean isEdgeNew(Edge newEdge) {
		for (Edge e : edges) {
			if (newEdge.equals(e))
				return false;
		}
		return true;
	}

	public void setOuter(boolean b) {
		outer = b;
	}

	public boolean isOuter() {
		return outer;
	}

	public void addVertex(Vertex v) {
		vertices.add(v);

	}

	public List<Vertex> getVertices() {
		return vertices;
	}

	public void addPivotList(int i, List<Integer> pivotList) {

	}

	public void findPossiblePolygons() {
		int pointsToReach = vertices.size();

		for (int i : pivotList) {
			int pointsReached = 0;
			int actualPosition = i;
			int amtLines = 0;
			while (pointsReached <= pointsToReach) {
				pointsReached += pivotList[actualPosition];
				actualPosition += pivotList[actualPosition];
				actualPosition %= vertices.size();
				amtLines++;
			}
			possiblePolygonStartPoints.add(amtLines);
		}
	}

	public void addPivotList(int[] pivotList) {
		this.pivotList = pivotList;
	}

	public void findOptimalStartposition() {
		int min = 0;
		int position = 0;
		for (int i = 0; i < possiblePolygonStartPoints.size(); i++) {
			if (i == 0) {
				min = possiblePolygonStartPoints.get(i);
				position = i;
			}
			if (possiblePolygonStartPoints.get(i) < min) {
				min = possiblePolygonStartPoints.get(i);
				position = i;
			}
		}
		optimalStartPosition = position;
	}

	public int[] getPivotList() {
		return pivotList;
	}

	public int getOptimalStartPosition() {
		return optimalStartPosition;
	}

	public List<Integer> getPossiblePolygonStartPoints() {
		return possiblePolygonStartPoints;
	}

}
