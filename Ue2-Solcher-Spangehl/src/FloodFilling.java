
// Copyright (C) 2014 by Klaus Jung
// All rights reserved.
// Date: 2014-10-02

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FloodFilling extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int border = 10;
	private static final int maxWidth = 400;
	private static final int maxHeight = 400;
	private static final int BLACK = Utils.ARGB_BLACK;
	private static final int WHITE = Utils.ARGB_WHITE;
	private static final File openPath = new File(".");
	private static final String title = "Binarisierung";
	private static final String author = "Spangehl";
	private static final String initalOpen = "sample.png";
	private static int memorySize = 0;

	static final int TH_MIN = 0;
	static final int TH_MAX = 255;
	static final int TH_INIT = 128;
	private int[] histogram = new int[256];
	private int[] originalPic;
	private List<Integer> colorList = new ArrayList<Integer>();

	private static JFrame frame;

	private static ImageView srcView; // source image view
	private static ImageView dstView; // binarized image view

	private JComboBox<String> methodList; // the selected binarization method
	private JLabel statusLine; // to print some status text

	public FloodFilling() {
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
		JButton load = new JButton("Bild öffnen");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File input = openFile();
				if (input != null) {
					srcView.loadImage(input);
					srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
					floodImage();
				}
			}
		});

		// selector for the binarization method
		JLabel methodText = new JLabel("Methode:");
		String[] methodNames = { "Depth First", "Breadth First", "Sequentiell", "Depth First Evolved",
				"Breadth First Evolved" };

		methodList = new JComboBox<String>(methodNames);
		methodList.setSelectedIndex(0); // set initial method
		methodList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				floodImage();
			}
		});

		// some status text
		statusLine = new JLabel("Wir suchen nach Bildregionen!");

		// arrange all controls
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, border, 0, 0);
		controls.add(load, c);
		controls.add(methodText, c);
		controls.add(methodList, c);

		JPanel images = new JPanel(new FlowLayout());
		images.add(srcView);
		images.add(dstView);

		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(statusLine, BorderLayout.SOUTH);
		frame.setSize(frame.getWidth() + 20, frame.getHeight());

		setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

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

		JComponent newContentPane = new FloodFilling();
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

	protected void floodImage() {

		String methodName = (String) methodList.getSelectedItem();

		// image dimensions
		int width = srcView.getImgWidth();
		int height = srcView.getImgHeight();

		// get pixels arrays
		int srcPixels[] = srcView.getPixels();
		int dstPixels[] = java.util.Arrays.copyOf(srcPixels, srcPixels.length);

		String message = "Auffinden von Bildregionen mit \"" + methodName + "\"";
		binarize(dstPixels, isoData(dstPixels, 128));
		statusLine.setText(message);

		long startTime = System.currentTimeMillis();
		String stackQueue = "";
		switch (methodList.getSelectedIndex()) {
		case 0: // DepthFirst

			depthFirst(dstPixels);
			stackQueue = "stack";
			break;
		case 1: // BreadthFirst
			breadthFirst(dstPixels);
			stackQueue = "queue";
			break;
		case 2: // Sequentiell
			colorList.clear();
			Set<int[]> collisionSet = seqRegion(dstPixels);
			Vector<Set<Integer>> colorRegions = resolveCollisions(collisionSet);
			relabelPixture(dstPixels, colorRegions);
			break;
		case 3:
			depthFirstEvolved(dstPixels);
			stackQueue = "stack";

			break;
		case 4:
			breadthFirstEvolved(dstPixels);
			stackQueue = "queue";

			break;
		}

		long time = System.currentTimeMillis() - startTime;

		dstView.setPixels(dstPixels, width, height);

		// dstView.saveImage("out.png");

		frame.pack();

		String text = message + " in " + time + " ms";
		if (memorySize > 0)
			statusLine.setText(text + ". The maximum size of the " + stackQueue + " was " + memorySize + ".");
		else
			statusLine.setText(text);
		memorySize = 0;
	}

	private void relabelPixture(int[] pixels, Vector<Set<Integer>> colorRegions) {
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] != WHITE) {
				Set<Integer> set = new HashSet<Integer>();
				for (Set<Integer> s : colorRegions) {
					for (int j : s) {
						if (j == pixels[i]) {
							set = s;
							break;
						}
					}
				}
				int min = 0;
				for (int j : set) {
					min = j;
					if (min > j)
						min = j;
				}
				pixels[i] = min;
			}
		}

	}

	private Vector<Set<Integer>> resolveCollisions(Set<int[]> collisionSet) {
		Vector<Set<Integer>> parts = new Vector<Set<Integer>>();
		for (int i : colorList) {
			Set<Integer> toAdd = new HashSet<Integer>();
			toAdd.add(i);
			parts.add(toAdd);
		}
		System.out.println(parts.size());
		System.out.println(collisionSet.size());

		for (int[] i : collisionSet) {

			int a = i[0];
			int b = i[1];
			int aSet = -1;
			int bSet = -1;
			for (int j = 0; j < parts.size(); j++) { //Set<Integer> j : parts) {
				for (int k : parts.get(j)) {
					if (k == a)
						aSet = j;
					if (k == b)
						bSet = j;
				}
				if (aSet != -1 && bSet != -1 && aSet != bSet) {
					parts.get(aSet).addAll(parts.get(bSet));
					parts.get(bSet).clear();
					break;
				}
			}
		}
		return parts;
	}

	private Set<int[]> seqRegion(int[] pixels) {
		boolean[] visited = new boolean[pixels.length];
		int rgb = Utils.getRandomColor();
		colorList.add(rgb);
		Set<int[]> set = new HashSet<int[]>();
		System.out.println("Height:" + srcView.getHeight());
		System.out.println("ImgHeight:" + srcView.getImgHeight());
		for (int i = 0; i < srcView.getImgHeight(); i++) {
			for (int j = 0; j < srcView.getImgWidth(); j++) {
				int currentPos = Utils.pixelPosSafe(j, i, srcView.getImgWidth(), srcView.getImgHeight());
				if (pixels[currentPos] == BLACK) {
					//visited[currentPos] = true;
					List<Integer> neighbourhood = getNeighboursInPicture(pixels, currentPos, 9, visited);
					int countColored = 0;
					for (int k : neighbourhood)
						if (pixels[k] != BLACK)
							countColored++;
					if (countColored == 0) {
						pixels[currentPos] = rgb;
						rgb = Utils.getRandomColor();
						colorList.add(rgb);
					} else if (countColored == 1) {
						for (int k : neighbourhood)
							if (pixels[k] != BLACK)
								pixels[currentPos] = pixels[k];
					} else if (countColored > 1) {
						for (int k : neighbourhood)
							if (pixels[k] != BLACK) {
								pixels[currentPos] = pixels[k];
								break;
							}
						for (int k : neighbourhood) {
							if (pixels[k] != BLACK && pixels[k] != pixels[currentPos]) {
								int[] colorNeighbor = { pixels[currentPos], pixels[k] };
								set.add(colorNeighbor);
							}
						}
					}

				}
			}
		}
		return set;

	}

	private void breadthFirstEvolved(int[] pixels) {
		boolean[] visited = new boolean[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == BLACK) {
				int rgb = Utils.getRandomColor();
				Queue<int[]> queue = new LinkedList<int[]>();
				if (!visited[i])
					queue.add(getPixelPos(i, srcView.getImgWidth()));
				visited[i] = true;
				while (!queue.isEmpty()) {
					pushQueueEvolved(pixels, rgb, visited, queue);
				}
			}
		}
	}

	private void pushQueueEvolved(int[] pixels, int rgb, boolean[] visited, Queue<int[]> queue) {
		int[] pos = queue.poll();
		int currentPos = Utils.pixelPosSafe(pos[0], pos[1], srcView.getImgWidth(), srcView.getImgHeight());
		if (pixels[currentPos] == BLACK && currentPos > 0
				&& currentPos < (srcView.getImgHeight() * srcView.getImgWidth())) {
			visited[currentPos] = true;
			pixels[currentPos] = rgb;
			List<Integer> neighbourhood = getNeighboursInPicture(pixels, currentPos, 9, visited);
			for (int j = 0; j < neighbourhood.size(); j++) {
				if (neighbourhood.get(j) != 0 && visited[neighbourhood.get(j)] == false) {
					visited[neighbourhood.get(j)] = true;
					queue.offer(getPixelPos(neighbourhood.get(j), srcView.getImgWidth()));
				}
			}
		}
		if (memorySize < queue.size())
			memorySize = queue.size();
	}

	private void depthFirstEvolved(int[] pixels) {
		boolean[] visited = new boolean[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == BLACK) {
				int rgb = Utils.getRandomColor();
				Stack<int[]> stack = new Stack<int[]>();
				if (!visited[i])
					stack.push(getPixelPos(i, srcView.getImgWidth()));
				visited[i] = true;
				while (!stack.empty()) {
					pushStackEvolved(pixels, rgb, visited, stack);
				}
			}
		}
	}

	private void depthFirst(int[] pixels) {
		boolean[] visited = new boolean[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == BLACK) {
				int rgb = Utils.getRandomColor();
				Stack<int[]> stack = new Stack<int[]>();
				if (!visited[i])
					stack.push(getPixelPos(i, srcView.getImgWidth()));
				visited[i] = true;
				while (!stack.empty()) {
					pushStack(pixels, rgb, visited, stack);
				}
			}
		}
	}

	private void pushStack(int[] pixels, int rgb, boolean[] visited, Stack<int[]> stack) {
		int[] pos = stack.pop();
		int currentPos = Utils.pixelPosSafe(pos[0], pos[1], srcView.getImgWidth(), srcView.getImgHeight());
		if (pixels[currentPos] == BLACK && currentPos > 0
				&& currentPos < (srcView.getImgHeight() * srcView.getImgWidth())) {
			pixels[currentPos] = rgb;
			visited[currentPos] = true;
			List<Integer> neighbourhood = getNeighbours(pixels, currentPos, 9);
			for (int j = 0; j < neighbourhood.size(); j++) {
				if (neighbourhood.get(j) != 0 && visited[neighbourhood.get(j)] == false) {
					visited[neighbourhood.get(j)] = true;
					stack.push(getPixelPos(neighbourhood.get(j), srcView.getImgWidth()));
				}
			}
		}
		if (memorySize < stack.size())
			memorySize = stack.size();
	}

	// pusht die nachbarschaftspixel in den stack
	private void pushStackEvolved(int[] pixels, int rgb, boolean[] visited, Stack<int[]> stack) {
		int[] pos = stack.pop();
		int currentPos = Utils.pixelPosSafe(pos[0], pos[1], srcView.getImgWidth(), srcView.getImgHeight());
		if (pixels[currentPos] == BLACK && currentPos > 0
				&& currentPos < (srcView.getImgHeight() * srcView.getImgWidth())) {
			visited[currentPos] = true;
			pixels[currentPos] = rgb;
			List<Integer> neighbourhood = getNeighboursInPicture(pixels, currentPos, 9, visited);
			for (int j = 0; j < neighbourhood.size(); j++) {
				if (neighbourhood.get(j) != 0 && visited[neighbourhood.get(j)] == false) {
					visited[neighbourhood.get(j)] = true;
					stack.push(getPixelPos(neighbourhood.get(j), srcView.getImgWidth()));
				}
			}
		}
		if (memorySize < stack.size())
			memorySize = stack.size();
	}

	private ArrayList<Integer> getNeighbours(int[] pixels, int pixelPos, int kernelSize) {
		int amountNeighbours = kernelSize - 1;
		ArrayList<Integer> neighbours = new ArrayList<Integer>();
		int r = (int) ((Math.sqrt(amountNeighbours)) / 2);
		int[] xY = getPixelPos(pixelPos, srcView.getImgWidth());
		int x = xY[0];
		int y = xY[1];
		for (int j = -r; j <= r; j++) {
			for (int i = -r; i <= r; i++) {
				int currWidth = x + i;
				int currHeight = y + j;
				if (i != currWidth && j != currHeight) {
					neighbours.add(
							Utils.pixelPosSafe(currWidth, currHeight, srcView.getImgWidth(), srcView.getImgHeight()));
				}
			}
		}
		return neighbours;
	}

	private List<Integer> getNeighboursInPicture(int[] pixels, int pixelPos, int kernelSize, boolean[] visited) {
		int amountNeighbours = kernelSize - 1;
		List<Integer> neighbours = new ArrayList<Integer>();
		int r = (int) ((Math.sqrt(amountNeighbours)) / 2);
		int[] xY = getPixelPos(pixelPos, srcView.getImgWidth());
		int x = xY[0];
		int y = xY[1];
		for (int j = -r; j <= r; j++) {
			for (int i = -r; i <= r; i++) {
				int currWidth = x + i;
				int currHeight = y + j;
				if (currWidth >= 0 && currHeight >= 0 && !(currWidth == x && currHeight == y)
						&& currWidth < srcView.getImgWidth() && currHeight < srcView.getImgHeight()) {
					int thisPixelPos = Utils.pixelPosSafe(currWidth, currHeight, srcView.getImgWidth(),
							srcView.getImgHeight());
					if (pixels[thisPixelPos] != WHITE && visited[thisPixelPos] == false) {
						neighbours.add(Utils.pixelPosSafe(currWidth, currHeight, srcView.getImgWidth(),
								srcView.getImgHeight()));
					}
				}
			}
		}
		return neighbours;
	}

	private void breadthFirst(int[] pixels) {
		boolean[] visited = new boolean[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == BLACK) {
				int rgb = Utils.getRandomColor();
				Queue<int[]> queue = new LinkedList<int[]>();
				if (!visited[i])
					queue.add(getPixelPos(i, srcView.getImgWidth()));
				visited[i] = true;
				while (!queue.isEmpty()) {
					pushQueue(pixels, rgb, visited, queue);
				}
			}
		}
	}

	private void pushQueue(int[] pixels, int rgb, boolean[] visited, Queue<int[]> queue) {
		int[] pos = queue.poll();
		int currentPos = Utils.pixelPosSafe(pos[0], pos[1], srcView.getImgWidth(), srcView.getImgHeight());
		if (pixels[currentPos] == BLACK && currentPos > 0
				&& currentPos < (srcView.getImgHeight() * srcView.getImgWidth())) {
			pixels[currentPos] = rgb;
			visited[currentPos] = true;
			List<Integer> neighbourhood = getNeighbours(pixels, currentPos, 9);
			for (int j = 0; j < neighbourhood.size(); j++) {
				if (neighbourhood.get(j) != 0 && visited[neighbourhood.get(j)] == false) {
					visited[neighbourhood.get(j)] = true;
					queue.offer(getPixelPos(neighbourhood.get(j), srcView.getImgWidth()));
				}
			}
		}
		if (memorySize < queue.size())
			memorySize = queue.size();
	}

	// berechnet x und y koordinaten for i in eindimensionalem array
	private int[] getPixelPos(int pixelPos, int width) {
		int[] xY = new int[2];
		xY[0] = pixelPos % width; // x
		xY[1] = pixelPos / width; // y
		return xY;
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