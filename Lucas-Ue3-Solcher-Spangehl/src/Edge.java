import java.security.InvalidParameterException;
import java.util.List;

public class Edge {

	private Vertex v1;
	private Vertex v2;
	private String direction;

	public Edge(Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
		setDirection();
	}
	
	//?
	public Edge(Vertex vertex) {

	}

	public Edge(List<Vertex> vertices) {
		if (vertices != null && vertices.size() >= 2) {
			v1 = vertices.get(0);
			v2 = vertices.get(1);
			//set direction()
		} else
			throw new InvalidParameterException();
		setDirection();
	}

	private void setDirection() {
		if (v2.getX() > v1.getX())
			direction = "right";
		else if (v2.getX() < v1.getX())
			direction = "left";
		else if (v2.getY() > v1.getY())
			direction = "down";
		else
			direction = "up";
		//isNoEdgeException
	}

	public String getDirection() {
		return direction;
	}

	public Vertex getV1() {
		return v1;
	}

	public void setV1(Vertex v1) {
		this.v1 = v1;
	}

	public Vertex getV2() {
		return v2;
	}

	public void setV2(Vertex v2) {
		this.v2 = v2;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Edge)) {
			return false;
		}
		Edge that = (Edge) other;

		return this.v1.equals(that.v1) && this.v2.equals(that.v2);
	}

}
