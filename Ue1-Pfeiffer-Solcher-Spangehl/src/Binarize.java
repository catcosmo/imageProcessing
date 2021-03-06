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
import java.util.concurrent.ThreadLocalRandom;

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

public class Binarize extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int border = 10;
	private static final int maxWidth = 400;
	private static final int maxHeight = 400;
	private static final File openPath = new File(".");
	private static final String title = "Binarisierung";
	private static final String author = "Solcher, Spangehl";
	private static final String initalOpen = "tools1.png";
	static final int TH_MIN = 0;
	static final int TH_MAX = 255;
	static final int TH_INIT = 128;
	private int[] histogram = new int[256];
	private int[] originalPic;

	private static JFrame frame;

	private static ImageView srcView; // source image view
	private static ImageView dstView; // binarized image view

	private JComboBox<String> methodList; // the selected binarization method
	private JLabel statusLine; // to print some status text
	private JSlider slider;
	private static int threshold = 128; // value for slider

	public Binarize() {
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
					binarizeImage();
				}
			}
		});

		// selector for the binarization method
		JLabel methodText = new JLabel("Methode:");
		String[] methodNames = { "Schwellwert Slider", "Iso-Data-Algorithmus",
				"Outline" };

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

		slider = new JSlider(JSlider.HORIZONTAL, TH_MIN, TH_MAX, TH_INIT);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				threshold = source.getValue();
				binarizeImage();

			}
		});
		// Turn on labels at major tick marks.
		slider.setMajorTickSpacing(64);
		slider.setMinorTickSpacing(8);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		// arrange all controls
		JPanel controls = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, border, 0, 0);
		controls.add(load, c);
		controls.add(methodText, c);
		controls.add(methodList, c);
		controls.add(slider, c);

		JPanel images = new JPanel(new FlowLayout());
		images.add(srcView);
		images.add(dstView);

		add(controls, BorderLayout.NORTH);
		add(images, BorderLayout.CENTER);
		add(statusLine, BorderLayout.SOUTH);

		setBorder(BorderFactory.createEmptyBorder(border, border, border,
				border));

		// perform the initial binarization
		binarizeImage();
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
		dstView.setPixels(originalPic, srcView.getImgWidth(),
				srcView.getImgHeight());
		frame.pack();

	}

	private File openFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
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

		JComponent newContentPane = new Binarize();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// display the window.
		frame.pack();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2,
				(screenSize.height - frame.getHeight()) / 2);
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

		switch (methodList.getSelectedIndex()) {
		case 0: // 50% Schwellwert
			slider.setVisible(true);
			binarize(dstPixels);
			break;
		case 1: // ISO-Data-Algorithmus
			int threshold = ThreadLocalRandom.current().nextInt(1, 255 + 1);
			slider.setVisible(false);
			threshold = isoData(srcPixels, threshold);
			binarize(dstPixels, threshold);
			break;
		case 2:
			slider.setVisible(true);
			binarize(dstPixels);
			outline(dstPixels);
			break;
		}

		long time = System.currentTimeMillis() - startTime;

		dstView.setPixels(dstPixels, width, height);

		// dstView.saveImage("out.png");

		frame.pack();

		statusLine.setText(message + " in " + time + " ms.");
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