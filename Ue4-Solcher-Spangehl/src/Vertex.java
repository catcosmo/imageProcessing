public class Vertex {

	private int x;
	private int y;

	public Vertex(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Vertex(Edge edge, String newDirection) {
		int lastX = edge.getV2().getX();
		int lastY = edge.getV2().getY();
		switch (newDirection) {
		case "left":
			this.x = lastX - 1;
			this.y = lastY;
			break;
		case "right":
			this.x = lastX + 1;
			this.y = lastY;
			break;
		case "up":
			this.x = lastX;
			this.y = lastY - 1;
			break;
		case "down":
			this.x = lastX;
			this.y = lastY + 1;
			break;
		}
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int[] getxY() {
		return new int[] { x, y };
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Vertex)) {
			return false;
		}
		Vertex that = (Vertex) other;

		return this.x == that.x && this.y == that.y;
	}
}
