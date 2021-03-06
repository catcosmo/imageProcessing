
// Copyright (C) 2014 by Klaus Jung
// All rights reserved.
// Date: 2014-10-02

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PathFinding extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int border = 10;
	private static final int maxWidth = 400;
	private static final int maxHeight = 400;
	private static final File openPath = new File(".");
	private static final String title = "Binarisierung";
	private static final String author = "Solcher, Spangehl";
	private static final String initalOpen = "klein.png";
	static final int TH_MIN = 0;
	static final int TH_MAX = 5;
	static final int TH_INIT = 1;
	private static final int BLACK = Utils.ARGB_BLACK;
	private static final int WHITE = Utils.ARGB_WHITE;

	private int[] histogram = new int[256];
	private int[] originalPic;

	private static JFrame frame;

	private static ImageView srcView; // source image view
	private static ImageView dstView; // binarized image view

	private JComboBox<String> methodList; // the selected binarization method
	private JLabel statusLine; // to print some status text
	private JSlider slider;
	private static int threshold = 128; // value for slider

	public PathFinding() {
		super(new BorderLayout(border, border));

		// load the default image
		File input = new File(initalOpen);

		if (!input.canRead())
			input = openFile(); // file not found, choose another image

		srcView = new ImageView(input);
		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
		// create an empty destination image
		dstView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));

		// load image button
		JButton load = new JButton("Bild �ffnen");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if (input != null) {
					srcView.loadImage(input);
					srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
					binarizeImage();
				}
			}
		});

		// selector for the binarization method
		JLabel methodText = new JLabel("Methode:");
		String[] methodNames = { "Schwellwert Slider", "Iso-Data-Algorithmus", "Outline", "Breadth-First" };

		methodList = new JComboBox<String>(methodNames);
		methodList.setSelectedIndex(0); // set initial method
		methodList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				binarizeImage();
			}
		});

		// some status text
		statusLine = new JLabel("The Threshold is");

		// slider for threshold
		final int sliderGranularity = 100;
		slider = new JSlider(JSlider.HORIZONTAL, TH_MIN * sliderGranularity, TH_MAX * sliderGranularity,
				TH_INIT * sliderGranularity);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				double val = (double) ((JSlider) e.getSource()).getValue() / sliderGranularity;
				binarizeImage();
				dstView.setZoom(val);
			}
		});
		// Turn on labels at major tick marks.
		slider.setMajorTickSpacing(1000);
		slider.setMinorTickSpacing(100);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		JButton btnPath = new JButton("Path");
		btnPath.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int srcPixels[] = srcView.getPixels();
				final int dstPixels[] = java.util.Arrays.copyOf(srcPixels, srcPixels.length);
				List<Path> pathList = path(dstPixels);
				findOptimalPaths(pathList);
				createPotraceLines(pathList);
				// createLines(pathList);

			}
		});

		// arrange all controls
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, border, 0, 0);
		controls.add(load, c);
		controls.add(methodText, c);
		controls.add(btnPath, c);
		controls.add(slider, c);

		JPanel images = new JPanel(new FlowLayout());
		images.add(srcView);
		images.add(dstView);

		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(statusLine, BorderLayout.SOUTH);

		setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

		// perform the initial binarization
		binarizeImage();
	}

	protected void createPotraceLines(List<Path> pathList) {
		Color c1 = Color.RED;
		Color c2 = Color.BLUE;
		Color c = c1;
		List<ArrayList<Line>> allLines = new ArrayList<ArrayList<Line>>();
		for (Path p : pathList) {
			if (p.isOuter())
				c = c1;
			else
				c = c2;
			ArrayList<Line> lineList = new ArrayList<Line>();
			int startPosition = p.getOptimalStartPosition();
			List<Vertex> vertexList = new ArrayList<Vertex>();
			vertexList.add(p.getVertices().get(startPosition));
			int actualPosition = startPosition;
			for (int i = 0; i < p.getPossiblePolygonStartPoints().get(startPosition); i++) {// p.getPivotList()[startPosition];// i++) {
				actualPosition += p.getPivotList()[actualPosition % p.getVertices().size()];
				vertexList.add(p.getVertices().get(actualPosition % p.getVertices().size()));
			}
			vertexList.add(p.getVertices().get(startPosition));
			boolean first = true;
			Vertex v1 = null;
			Vertex v2;
			for (Vertex e : vertexList) {
				if (first) {
					first = false;
					v1 = e;
				} else {
					v2 = e;
					Line line = new Line(v1.getX(), v2.getX(), v1.getY(), v2.getY(), c);
					lineList.add(line);
					v1 = v2;
				}
			}
			allLines.add(lineList);
		}
		dstView.setLines(allLines);

	}

	protected void findOptimalPaths(List<Path> pathList) {
		for (Path p : pathList) {
			int[] pivotList = new int[p.getVertices().size()];
			for (int i = 0; i < p.getVertices().size(); i++) {
				int pivotElement = findStraightPath(i, p);
				if (i == p.getVertices().size() - 1)
					pivotList[0] = pivotElement;
				else
					pivotList[i + 1] = pivotElement;

			}
			p.addPivotList(pivotList);
			p.findPossiblePolygons();
			p.findOptimalStartposition();
		}
	}

	private int findStraightPath(int position, Path p) {
		List<Vertex> vertices = p.getVertices();
		Vertex c0 = new Vertex(0, 0);
		Vertex c1 = new Vertex(0, 0);
		Vertex v = vertices.get(position);
		boolean stop = false;
		int count = 0;
		Set<String> directions = new HashSet<String>();
		while (!stop) {
			String direction = p.getEdges().get((position + count) % p.getEdges().size()).getDirection();
			directions.add(direction);
			if (directions.size() > 3)
				stop = true;
			if (!stop) {
				count++;
				Vertex nextVector = vertices.get((position + count) % vertices.size());
				Vertex result = new Vertex(nextVector.getX() - v.getX(), nextVector.getY() - v.getY());
				if (contraintsViolated(result, c0, c1))
					stop = true;
				else
					checkConstraints(result, c0, c1);
			}
		}
		return count - 2;
	}

	private void checkConstraints(Vertex result, Vertex c0, Vertex c1) {
		if (Math.abs(result.getX()) <= 1 && Math.abs(result.getY()) <= 1)
			;
		else
			updateConstraints(result, c0, c1);
	}

	private void updateConstraints(Vertex result, Vertex c0, Vertex c1) {
		int dx = 0;
		int dy = 0;
		if (result.getY() >= 0 && (result.getY() > 0 || result.getX() < 0)) {
			dx = result.getX() + 1;
		} else {
			dx = result.getX() - 1;
		}
		if (result.getX() <= 0 && (result.getX() < 0 || result.getY() < 0)) {
			dy = result.getY() + 1;
		} else {
			dy = result.getY() - 1;
		}
		int crossProduct = c0.getX() * dy - c0.getY() * dx;
		if (crossProduct >= 0) {
			c0.setX(dx);
			c0.setY(dy);
		}
		dx = 0;
		dy = 0;
		if (result.getY() <= 0 && (result.getY() < 0 || result.getX() < 0)) {
			dx = result.getX() + 1;
		} else {
			dx = result.getX() - 1;
		}
		if (result.getX() >= 0 && (result.getX() > 0 || result.getY() < 0)) {
			dy = result.getY() + 1;
		} else {
			dy = result.getY() - 1;
		}
		crossProduct = c1.getX() * dy - c1.getY() * dx;
		if (crossProduct <= 0) {
			c1.setX(dx);
			c1.setY(dy);
		}
	}

	private boolean contraintsViolated(Vertex result, Vertex c0, Vertex c1) {
		int crossProduct0 = c0.getX() * result.getY() - c0.getY() * result.getX();
		int crossProduct1 = c1.getX() * result.getY() - c1.getY() * result.getX();
		if (crossProduct0 < 0 || crossProduct1 > 0)
			return true;
		return false;
	}

	protected void createLines(List<Path> pathList) {
		Color c1 = Color.RED;
		Color c2 = Color.BLUE;
		Color c = c1;
		List<ArrayList<Line>> allLines = new ArrayList<ArrayList<Line>>();
		for (Path p : pathList) {
			if (p.isOuter())
				c = c1;
			else
				c = c2;
			ArrayList<Line> lineList = new ArrayList<Line>();
			for (Edge e : p.getEdges()) {
				Line line = new Line(e.getV1().getX(), e.getV2().getX(), e.getV1().getY(), e.getV2().getY(), c);
				lineList.add(line);
			}
			allLines.add(lineList);
		}
		dstView.setLines(allLines);

	}

	protected void outline(int[] pixels) {
		String message = "Outline";

		statusLine.setText(message);

		long startTime = System.currentTimeMillis();
		erodeAndIntersect(pixels);
		long time = System.currentTimeMillis() - startTime;
		frame.pack();

		statusLine.setText(message + " in " + time + " ms");

	}

	private void erodeAndIntersect(int[] pixels) {
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();
		Utils.invertImage(pixels);
		int srcPixels[] = java.util.Arrays.copyOf(pixels, pixels.length);
		int dstPixels[] = java.util.Arrays.copyOf(pixels, pixels.length);
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				Utils.dilate(srcPixels, dstPixels, width, height, 1.0f, i, j);
		Utils.invertImage(pixels);
		intersection(pixels, dstPixels);

	}

	private void intersection(int[] pixels, int[] erodedPic) {
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] != erodedPic[i])
				pixels[i] = 0xffffffff;
		}

	}

	protected void reverse() {
		dstView.setPixels(originalPic, srcView.getImgWidth(), srcView.getImgHeight());
		frame.pack();

	}

	private File openFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png",
				"gif");
		chooser.setFileFilter(filter);
		chooser.setCurrentDirectory(openPath);
		int ret = chooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			frame.setTitle(title + chooser.getSelectedFile().getName());
			return chooser.getSelectedFile();
		}
		return null;
	}

	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame(title + " - " + author + " - " + initalOpen);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JComponent newContentPane = new PathFinding();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// display the window.
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	protected void binarizeImage() {

		String methodName = (String) methodList.getSelectedItem();

		// image dimensions
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();

		// get pixels arrays
		int srcPixels[] = srcView.getPixels();
		int dstPixels[] = java.util.Arrays.copyOf(srcPixels, srcPixels.length);

		String message = "Binarisieren mit \"" + methodName + "\"";

		statusLine.setText(message);

		long startTime = System.currentTimeMillis();

		long time = System.currentTimeMillis() - startTime;

		dstView.setPixels(dstPixels, width, height);

		// dstView.saveImage("out.png");

		frame.pack();

		statusLine.setText(message + " in " + time + " ms.");
	}

	private List<Path> path(int[] dstPixels) {

		boolean[] visitedNormal = new boolean[dstPixels.length];
		boolean[] visitedInverted = new boolean[dstPixels.length];

		List<Path> paths = new ArrayList<Path>();
		long time = System.currentTimeMillis();
		for (int i = 0; i < dstPixels.length; i++) {
			if (dstPixels[i] == BLACK && visitedNormal[i] == false) {
				Path path = findPath(i, dstPixels);
				System.out.println(System.currentTimeMillis() - time);
				path.setOuter(true);
				paths.add(path);
				visitedNormal = findEnclosedPixels(dstPixels, path, visitedNormal);
				System.out.println(System.currentTimeMillis() - time);
			}
		}
		for (int i = 0; i < dstPixels.length; i++) {
			if (dstPixels[i] == BLACK && visitedInverted[i] == false) {
				Path path = findPath(i, dstPixels);
				System.out.println(System.currentTimeMillis() - time);
				paths.add(path);
				path.setOuter(false);
				visitedInverted = findEnclosedPixels(dstPixels, path, visitedInverted);
				System.out.println(System.currentTimeMillis() - time);
				// i = dstPixels.length;
			}
		}
		return paths;
	}

	private boolean[] findEnclosedPixels(int[] dstPixels, Path path, boolean[] visited) {
		long time = System.currentTimeMillis();
		GeneralPath gp = new GeneralPath();
		System.out.println("timeBefore: " + (System.currentTimeMillis() - time));

		gp.moveTo(path.getEdges().get(0).getV1().getX(), path.getEdges().get(0).getV1().getY());
		System.out.println("firstStep: " + (System.currentTimeMillis() - time));
		for (int i = 0; i < path.getEdges().size(); i++) {
			gp.lineTo(path.getEdges().get(i).getV2().getX(), path.getEdges().get(i).getV2().getY());
		}
		System.out.println("secondStep: " + (System.currentTimeMillis() - time));
		/*
		 * for (int i = (int) gp.getBounds().getMinY(); i <= gp.getBounds()
		 * .getMaxY() - gp.getBounds().getMinY(); i++) { for (int j = (int)
		 * gp.getBounds().getMinX(); j <= gp.getBounds() .getMaxX() -
		 * gp.getBounds().getMinX(); j++) { if (gp.contains(j, i)) { int
		 * position = j + dstView.getImgWidth() * i; visited[position] = true;
		 * if (dstPixels[position] == BLACK) dstPixels[position] = WHITE; else
		 * dstPixels[position] = BLACK; } } }
		 */

		for (int i = (int) (gp.getBounds().getMinY() * dstView.getImgWidth() + gp.getBounds().getMinX()); i < gp
				.getBounds().getMaxX() + gp.getBounds().getMaxY() * srcView.getImgWidth(); i++) {
			int[] xY = getPixelPos(i, srcView.getImgWidth());
			if (gp.contains(xY[0], xY[1])) {
				visited[i] = true;
				if (dstPixels[i] == BLACK)
					dstPixels[i] = WHITE;
				else
					dstPixels[i] = BLACK;
			}
		}

		System.out.println("finished: " + (System.currentTimeMillis() - time));

		return visited;

	}

	private int[] getPixelPos(int pixelPos, int width) {
		int[] xY = new int[2];
		xY[0] = pixelPos % width; // x
		xY[1] = pixelPos / width; // y
		return xY;
	}

	private Path findPath(int pixelPos, int[] dstPixels) {
		Path path = new Path();
		Pixel pixel = new Pixel(pixelPos, srcView.getImgWidth());
		path.addPixel(pixel);
		Vertex v1 = new Vertex(pixel.getX(), pixel.getY());
		Vertex v2 = new Vertex(pixel.getX(), pixel.getY() + 1);
		path.addVertex(v1);
		path.addVertex(v2);
		// pixel.addVertex(new Vertex(pixel.getX(), pixel.getY()));
		// pixel.addVertex(new Vertex(pixel.getX(), pixel.getY() + 1));
		Edge lastEdge = new Edge(v1, v2);
		path.addEdge(lastEdge);
		boolean stop = false;
		// System.out.println("x: " + v1.getX() + ", y:" + v1.getY());
		// System.out.println("x: " + v2.getX() + ", y:" + v2.getY());
		// Vertex vertex = getVertex(edge.getDirection());
		while (!stop) {
			lastEdge = path.getEdges().get(path.getEdges().size() - 1);
			List<Pixel> pixels = getPossiblePixels(lastEdge.getDirection(), pixel, dstPixels);
			String newDirection = decideDirection(lastEdge.getDirection(), pixels, pixel);
			Vertex vertex = new Vertex(lastEdge, newDirection);
			Edge newEdge = new Edge(lastEdge.getV2(), vertex);
			pixel = new Pixel(lastEdge.getDirection(), newDirection, pixel);
			if (path.isEdgeNew(newEdge)) {
				path.addEdge(newEdge);
				path.addVertex(newEdge.getV2());
			} else
				stop = !stop;
			// System.out.println("x: " + vertex.getX() + ", y:" +
			// vertex.getY());
		}
		return path;
	}

	private String decideDirection(String lastDirection, List<Pixel> pixels, Pixel lastPixel) {
		String newDirection = "";
		if (pixels.size() == 1) {
			Pixel thisPixel = pixels.get(0);
			if (lastPixel.getX() == thisPixel.getX() || lastPixel.getY() == thisPixel.getY()) {
				newDirection = lastDirection;
			} else {
				if (lastDirection.equals("right")) {
					newDirection = "down";
				} else if (lastDirection.equals("left")) {
					newDirection = "up";
				} else if (lastDirection.equals("up")) {
					newDirection = "right";
				} else if (lastDirection.equals("down")) {
					newDirection = "left";
				}
			}
		} else if (pixels.size() == 0) {
			if (lastDirection.equals("right")) {
				newDirection = "up";
			} else if (lastDirection.equals("left")) {
				newDirection = "down";
			} else if (lastDirection.equals("up")) {
				newDirection = "left";
			} else if (lastDirection.equals("down")) {
				newDirection = "right";
			}
		} else {
			if (lastDirection.equals("right")) {
				newDirection = "down";
			} else if (lastDirection.equals("left")) {
				newDirection = "up";
			} else if (lastDirection.equals("up")) {
				newDirection = "right";
			} else if (lastDirection.equals("down")) {
				newDirection = "left";
			}
		}
		return newDirection;
	}

	private List<Pixel> getPossiblePixels(String direction, Pixel pixel, int[] dstPixels) {
		Pixel p1 = null;
		Pixel p2 = null;
		switch (direction) {
		case "left":
			p1 = new Pixel(pixel.getX() - 1, pixel.getY(), true);
			p2 = new Pixel(pixel.getX() - 1, pixel.getY() - 1, true);
			break;
		case "right":
			p1 = new Pixel(pixel.getX() + 1, pixel.getY(), true);
			p2 = new Pixel(pixel.getX() + 1, pixel.getY() + 1, true);
			break;
		case "up":
			p1 = new Pixel(pixel.getX(), pixel.getY() - 1, true);
			p2 = new Pixel(pixel.getX() + 1, pixel.getY() - 1, true);
			break;
		case "down":
			p1 = new Pixel(pixel.getX(), pixel.getY() + 1, true);
			p2 = new Pixel(pixel.getX() - 1, pixel.getY() + 1, true);
			break;
		}
		List<Pixel> pixRet = new ArrayList<Pixel>();
		boolean p1InBoundaries = false;
		boolean p2InBoundaries = false;
		if (p1.getX() > 0 && p1.getX() < srcView.getImgWidth() && p1.getY() > 0 && p1.getY() < srcView.getImgHeight())
			p1InBoundaries = true;
		if (p2.getX() > 0 && p2.getX() < srcView.getImgWidth() && p2.getY() > 0 && p2.getY() < srcView.getImgHeight())
			p2InBoundaries = true;
		if (p1InBoundaries && dstPixels[Utils.pixelPos(p1.getX(), p1.getY(), srcView.getImgWidth())] == BLACK) {
			if (p2InBoundaries && dstPixels[Utils.pixelPos(p2.getX(), p2.getY(), srcView.getImgWidth())] == BLACK) {
				pixRet.add(p1);
				pixRet.add(p2);
			} else
				pixRet.add(p1);
		} else if (p2InBoundaries && dstPixels[Utils.pixelPos(p2.getX(), p2.getY(), srcView.getImgWidth())] == BLACK)
			pixRet.add(p2);

		return pixRet;
	}

	private void doQueue(int[] dstPixels, int x, int y, int label) {
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(x + y * srcView.getWidth());
		while (!queue.isEmpty()) {
			int point = queue.remove();
			if (point > 0 && point < dstPixels.length - 1 && dstPixels[x] == Utils.ARGB_BLACK) {
				dstPixels[x] = 2;
				queue.add(x + 1 + y * srcView.getWidth());
				queue.add(x + (y + 1) * srcView.getWidth());
				queue.add(x + (y - 1) * srcView.getWidth());
				queue.add(x - 1 + y * srcView.getWidth());

			}

		}

	}

	private void binarize(int pixels[]) {
		for (int i = 0; i < pixels.length; i++) {
			int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
			pixels[i] = gray < threshold ? 0xff000000 : 0xffffffff;
		}
	}

	private int isoData(int[] pixels, int initialThreshold) {
		createHist(pixels);
		int newThreshold = 0;
		double weightedSumLower = 0;
		double pointsLower = 0;
		for (int i = 0; i < initialThreshold; i++) {
			weightedSumLower += histogram[i] * i;
			pointsLower += histogram[i];
		}
		double weightedSumUpper = 0;
		double pointsUpper = 0;
		for (int i = initialThreshold; i < histogram.length; i++) {
			weightedSumUpper += histogram[i] * i;
			pointsUpper += histogram[i];
		}
		double meanLower = weightedSumLower / pointsLower;
		double meanUpper = weightedSumUpper / pointsUpper;
		newThreshold = (int) (meanLower + meanUpper) / 2;
		if (newThreshold != initialThreshold)
			return isoData(pixels, newThreshold);
		return newThreshold;

	}

	private void createHist(int[] pixels) {
		for (int i = 0; i < pixels.length; i++) {
			int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
			histogram[gray]++;
		}
	}

	void binarize(int pixels[], int treshold) {
		for (int i = 0; i < pixels.length; i++) {
			int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
			pixels[i] = gray < treshold ? 0xff000000 : 0xffffffff;
		}
	}

}