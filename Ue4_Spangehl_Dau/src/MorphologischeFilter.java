// BV Ue1 WS2014/15 Vorgabe
//
// Copyright (C) 2014 by Klaus Jung

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class MorphologischeFilter extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final String author = "Cat Spangehl & Julien Dau";	// TODO: type in your name here
	private static final String initialFilename = "rhino_part.png";
	private static final File openPath = new File(".");
	private static final int borderWidth = 5;
	private static final int maxWidth = 445;
	private static final int maxHeight = maxWidth;
	private static final int maxNoise = 30;	// in per cent
	
	private static JFrame frame;
	
	private ImageView srcView;			// source image view
	private ImageView dstView;			// filtered image view

	private int[] origPixels = null;
	private int[] pixelsBW = null;
	private int[] pixelsED1 = null;
	private int[] pixelsED2 = null;
	
	private int imageWidth;
	private int imageHeight;
	
	private JLabel statusLine = new JLabel("    "); // to print some status text
	
	private JComboBox<String> noiseType;

	private int threshold = 128;
	private int erosionDilation1 = 0;
	private int erosionDilation2 = 0;
	private JLabel 	thresholdLabel;
	private JLabel 	thresholdAmountLabel;
	private JSlider thresholdSlider;
	private JSlider erosionDilationSlider1;
	private JSlider erosionDilationSlider2;
	
	private JComboBox<String> filterType;

	public MorphologischeFilter() {
        super(new BorderLayout(borderWidth, borderWidth));

        setBorder(BorderFactory.createEmptyBorder(borderWidth,borderWidth,borderWidth,borderWidth));
 
        // load the default image
        File input = new File(initialFilename);
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        srcView = new ImageView(input);
        srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
        
        imageWidth = srcView.getImgWidth();
        imageHeight = srcView.getImgHeight();

        // keep a copy of the grayscaled original image pixels
        origPixels = srcView.getPixels().clone();
        
        pixelsBW  = new int[imageWidth*imageHeight];
        pixelsED1 = new int[imageWidth*imageHeight];
        pixelsED2 = new int[imageWidth*imageHeight];
       
		// create empty destination image of same size
		dstView = new ImageView(srcView.getImgWidth(), srcView.getImgHeight());
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));
		
        // control panel
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,borderWidth,0,0);
        
        // filter panel
        JPanel filters = new JPanel(new GridBagLayout());
        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(borderWidth, 0, borderWidth, 0);

		// load image button
        JButton load = new JButton("Open Image");
        load.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		loadFile(openFile());
                origPixels = srcView.getPixels().clone();
        		calculate();
        	}        	
        });
        
        // filter slider
        erosionDilationSlider1 = new JSlider(JSlider.HORIZONTAL, -50, 50, erosionDilation1);
        erosionDilationSlider2 = new JSlider(JSlider.HORIZONTAL, -50, 50, erosionDilation2);
      
        
        // threshold
        thresholdLabel = new JLabel("Threshold:");
        thresholdAmountLabel = new JLabel("" + threshold);
        thresholdSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, threshold);
        thresholdSlider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		threshold = thresholdSlider.getValue();
        		thresholdAmountLabel.setText("" + threshold);
        		calculate();
        	}
        });
        
        // e/d filter 1
        erosionDilationSlider1.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		erosionDilation1 = erosionDilationSlider1.getValue();
//        		thresholdAmountLabel.setText("" + threshold);
        		calculate();
        	}
        });
        
        // e/d filter 2
        erosionDilationSlider2.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {
        		erosionDilation2 = erosionDilationSlider2.getValue();
//        		thresholdAmountLabel.setText("" + threshold);
        		calculate();
        	}
        });
        
        controls.add(load, c);
        controls.add(thresholdLabel, c);
        controls.add(thresholdSlider, c);
        controls.add(thresholdAmountLabel, c);
        
        filters.add(erosionDilationSlider1, f);
        filters.add(erosionDilationSlider2, f);
        
        // images panel
        JPanel images = new JPanel(new GridLayout(1,1));
        //images.add(srcView);
        images.add(dstView);
        
        // status panel
        JPanel status = new JPanel(new GridBagLayout());
        
        status.add(statusLine, c);
        
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH); // TODO gib mir meinen status, baby!
        add(filters, BorderLayout.SOUTH);
        
        calculate();
                       
	}
	
	private File openFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(openPath);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;		
	}
	
	private void loadFile(File file) {
		if(file != null) {
    		srcView.loadImage(file);
    		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
    		// create empty destination image of same size
    		dstView.resetToSize(srcView.getImgWidth(), srcView.getImgHeight());
    		frame.pack();
		}
		
	}
    
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Nonlinear Filters - " + author);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent newContentPane = new MorphologischeFilter();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	private void calculate() {
		long startTime = System.currentTimeMillis();
			
		// start with original image pixels
		srcView.setPixels(origPixels);

		pixelsBW = Utils.makeBW(srcView.getPixels(), threshold);
		
		Utils.initWhite(pixelsED1);
		Utils.initWhite(pixelsED2);
		
		// Slider 1
		if (erosionDilation1 < 0) {
			erode(pixelsBW, pixelsED1, (-1*erosionDilation1)/10f);
		}
		else {
			dilate(pixelsBW, pixelsED1, erosionDilation1/10f);
		}
		
		// Slider 2
		if (erosionDilation2 < 0) {
			erode(pixelsED1, pixelsED2, (-1*erosionDilation2)/10f);
		}
		else {
			dilate(pixelsED1, pixelsED2, erosionDilation2/10f);
		}
		
		dstView.setPixels(pixelsED2);
		
		// make changes visible
		dstView.applyChanges();
		
		long time = System.currentTimeMillis() - startTime;
    	statusLine.setText("Processing Time = " + time + " ms");
	}
	
	private void dilate(int[] srcPixels, int[] dstPixels, float radius) {
		for(int y=0; y<imageHeight; y++) { // Walk the rows (y)
			for(int x=0; x<imageWidth; x++) { // Walk the columns (x)		
				Utils.dilate(srcPixels, dstPixels, imageWidth, imageHeight, radius, x, y);
			}
		}
	}
	
	private void erode(int[] srcPixels, int[] dstPixels, float radius) {
		Utils.invertImage(srcPixels);

		for(int y=0; y<imageHeight; y++) { // Walk the rows (y)
			for(int x=0; x<imageWidth; x++) { // Walk the columns (x)		
				Utils.dilate(srcPixels, dstPixels, imageWidth, imageHeight, radius, x, y);
			}
		}
		
		Utils.invertImage(dstPixels);
	}

	
	private void copyImage(int src[], int dst[]) {
		for(int i = 0; i < src.length; i++){
			dst[i] = src[i];
		}
	}

}
