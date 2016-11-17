import java.util.ArrayList;
import java.util.List;

public class Path {

	List<Pixel> pixels = new ArrayList<Pixel>();
	List<Edge> edges = new ArrayList<Edge>();

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

}
