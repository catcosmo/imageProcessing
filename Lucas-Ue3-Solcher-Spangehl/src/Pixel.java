import java.util.ArrayList;
import java.util.List;

public class Pixel {
	private int x;
	private int y;
	private List<Vertex> vertices = new ArrayList<Vertex>();

	/**
	 * Creates a pixel with xy-coordinates given
	 * @param x
	 * @param y
	 * @param isXY
	 */
	public Pixel(int x, int y, boolean isXY) {
		this.x = x;
		this.y = y;
	}

	public Pixel(int pixelPos, int imgWidth) {
		int[] xY = getPixelPos(pixelPos, imgWidth);
		x = xY[0];
		y = xY[1];
		/*
		 * vertices.add(new Vertex(x, y)); vertices.add(new Vertex(x + 1, y));
		 * vertices.add(new Vertex(x, y + 1)); vertices.add(new Vertex(x + 1, y
		 * + 1));
		 */

	}

	public Pixel(String lastDirection, String newDirection, Pixel pixel) {
		switch (lastDirection) {
		case "left":
			switch (newDirection) {
			case "left":
				x = pixel.getX() - 1;
				y = pixel.getY();
				break;
			case "up":
				x = pixel.getX() - 1;
				y = pixel.getY() - 1;
				break;
			case "down":
				x = pixel.getX();
				y = pixel.getY();
				break;
			}
			break;
		case "right":
			switch (newDirection) {
			case "right":
				x = pixel.getX() + 1;
				y = pixel.getY();
				break;
			case "up":
				x = pixel.getX();
				y = pixel.getY();
				break;
			case "down":
				x = pixel.getX() + 1;
				y = pixel.getY() + 1;
				break;
			}
			break;
		case "up":
			switch (newDirection) {
			case "left":
				x = pixel.getX();
				y = pixel.getY();
				break;
			case "right":
				x = pixel.getX() + 1;
				y = pixel.getY() - 1;
				break;
			case "up":
				x = pixel.getX();
				y = pixel.getY() - 1;
				break;
			}
			break;
		case "down":
			switch (newDirection) {
			case "left":
				x = pixel.getX() - 1;
				y = pixel.getY() + 1;
				break;
			case "right":
				x = pixel.getX();
				y = pixel.getY();
				break;
			case "down":
				x = pixel.getX();
				y = pixel.getY() + 1;
				break;
			}
			break;
		}
	}

	private int[] getPixelPos(int pixelPos, int width) {
		int[] xY = new int[2];
		xY[0] = pixelPos % width; // x
		xY[1] = pixelPos / width; // y
		return xY;
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

	public List<Vertex> getVertices() {
		return vertices;
	}

	public void setVertices(List<Vertex> vertices) {
		this.vertices = vertices;
	}

	public void addVertex(Vertex vertex) {
		vertices.add(vertex);
	}
}
