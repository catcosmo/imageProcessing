// Copyright (C) 2014 by Klaus Jung
// All rights reserved.
// Date: 2014-10-02

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

public class Binarize extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final int border = 10;
	private static final int maxWidth = 400;
	private static final int maxHeight = 400;
	private static final File openPath = new File(".");
	private static final String title = "Binarisierung";
	private static final String author = "Spangehl";	
	private static final String initalOpen = "tools1.png";
	static final int TH_MIN = 0;
	static final int TH_MAX = 255;
	static final int TH_INIT = 128;
	
	private static JFrame frame;
	
	private static ImageView srcView;				// source image view
	private static ImageView dstView;				// binarized image view
	
	private JComboBox<String> methodList;	// the selected binarization method
	private JLabel statusLine;				// to print some status text
	private JSlider slider;
	private static JCheckBox outlineBox = new JCheckBox("Outline");
	private static int threshold = 128; //value for slider

		

	public Binarize() {
        super(new BorderLayout(border, border));
        
        // load the default image
        File input = new File(initalOpen);
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
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
        		if(input != null) {
	        		srcView.loadImage(input);
	        		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
	                binarizeImage();
        		}
        	}        	
        });
         
        // selector for the binarization method
        JLabel methodText = new JLabel("Methode:");
        String[] methodNames = {"50% Schwellwert", "Iso-Data-Algorithmus"};
        
        methodList = new JComboBox<String>(methodNames);
        methodList.setSelectedIndex(0);		// set initial method
        methodList.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                binarizeImage();
        	}
        });
        
        // some status text
        statusLine = new JLabel("The Threshold is");
        
        //slider for threshold

        slider = new JSlider(JSlider.HORIZONTAL,TH_MIN, TH_MAX, TH_INIT);
		slider.addChangeListener(new ChangeListener() {
        	@Override
        	public void stateChanged(ChangeEvent e){
        		JSlider source = (JSlider) e.getSource();
        		threshold = source.getValue();
        		binarizeImage();

        	}
        });
        //Turn on labels at major tick marks.
    	slider.setMajorTickSpacing(64);
    	slider.setMinorTickSpacing(8);
    	slider.setPaintTicks(true);
    	slider.setPaintLabels(true);

		//Checkbox
	    ActionListener actionListener = new ActionListener() {
	        public void actionPerformed(ActionEvent actionEvent) {
	          AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
	          boolean selected = abstractButton.getModel().isSelected();
	          System.out.println(selected);
	        }
	    };
	    outlineBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
		        int width = srcView.getImgWidth();
		        int height = srcView.getImgHeight();
		    	
		    	// get pixels arrays
		    	int srcPixels[] = srcView.getPixels();
		    	int dstPixels[] = java.util.Arrays.copyOf(srcPixels, srcPixels.length);
				if(outlineBox.isSelected()){
			    	binarize(srcPixels);
					outline(srcPixels, dstPixels, height, width);
				}
				else
					reverse();
		        dstView.setPixels(dstPixels, width, height);
				revalidate();
			}
		});
    	
        
        // arrange all controls
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,border,0,0);
        controls.add(load, c);
        controls.add(methodText, c);
        controls.add(methodList, c);
        controls.add(slider, c);
        controls.add(outlineBox, c);
        
        JPanel images = new JPanel(new FlowLayout());
        images.add(srcView);
        images.add(dstView);
        
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(statusLine, BorderLayout.SOUTH);
               
        setBorder(BorderFactory.createEmptyBorder(border,border,border,border));
        
        // perform the initial binarization
        binarizeImage();
	}
	

	protected void reverse() {
		// TODO Auto-generated method stub
		
	}


	private File openFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(openPath);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) {
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
	
	
    protected void binarizeImage() {
  
        String methodName = (String)methodList.getSelectedItem();
        
        // image dimensions
        int width = srcView.getImgWidth();
        int height = srcView.getImgHeight();
    	
    	// get pixels arrays
    	int srcPixels[] = srcView.getPixels();
    	int dstPixels[] = java.util.Arrays.copyOf(srcPixels, srcPixels.length);
    	
    	String message = "Binarisieren mit \"" + methodName + "\"";

    	statusLine.setText(message);

		long startTime = System.currentTimeMillis();
		
    	switch(methodList.getSelectedIndex()) {
    	case 0:	// 50% Schwellwert
    		binarize(dstPixels);
    		break;
    	case 1:	// ISO-Data-Algorithmus
    		isoBinarize(dstPixels, isoData(128));
    		break;
    	}

		long time = System.currentTimeMillis() - startTime;
		   	
        dstView.setPixels(dstPixels, width, height);
        
        //dstView.saveImage("out.png");
    	
        frame.pack();
        
    	statusLine.setText(message + " in " + time + " ms");
    }
    
	private static void dilate(int[] srcPixels, int[] dstPixels, float radius, int width, int height) {
		for(int y=0; y<height; y++) { // Walk the rows (y)
			for(int x=0; x<width; x++) { // Walk the columns (x)		
				Utils.dilate(srcPixels, dstPixels, width, height, radius, x, y);
			}
		}
	}
	
	private static void erode(int[] srcPixels, int[] dstPixels, float radius, int width, int height) {
		Utils.invertImage(srcPixels);

		for(int y=0; y<height; y++) { // Walk the rows (y)
			for(int x=0; x<width; x++) { // Walk the columns (x)		
				Utils.dilate(srcPixels, dstPixels, width, height, radius, x, y);
			}
		}
		
		Utils.invertImage(dstPixels);
	}
    
    public static void outline(int[] srcPixels, int[] dstPixels, int height, int width){
    		int[] erodedPxls = srcPixels.clone();
    		//erodieren und in dst speichern
    		erode(srcPixels, erodedPxls, 1.0f, height, width);
    		// src invertieren
    		int[] invertedPxls = srcPixels.clone();
    		Utils.invertImage(invertedPxls);
    		//schnittmenge bilden und in dst abbilden
    		schnittmenge(erodedPxls, invertedPxls, dstPixels);
    }
    
    //returns array with schnittmenge for binarized pixelarrays
    public static void schnittmenge(int[] pixelsA, int[] pixelsB, int[] dstPixels){
    	for(int i=0; i<pixelsA.length; i++){
    		if(pixelsA[i] == 0xff000000 && pixelsB[i] == 0xff000000){
    			dstPixels[i] = 0xffffffff;}
    			else dstPixels[i] = 0xff000000;		
    			//hier 0xff000000 weiss und 0xffffffff schwarz weil, laut binarize
    	}
    }

    
    void binarize(int pixels[]) {
    	for(int i = 0; i < pixels.length; i++) {
    		int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
    		pixels[i] = gray < threshold ? 0xff000000 : 0xffffffff;

    	}
    }
    
    
    
    public static int isoData(int initialThresh){
        int[] origPixels = srcView.getPixels();
        int[] histogram = new int[256];
        for (int i=0; i<origPixels.length; ++i) {
        	int[] rgb = Utils.pixel2RGB(origPixels[i]);
        	int greyValue = (rgb[0] + rgb[1] + rgb[2]) / 3; 			// convert to greyscale if necessary
    		++histogram[greyValue];
        }
        int[] lowerHist = new int[initialThresh];
        int[] upperHist = new int[256-initialThresh];
        for(int i = 0; i<initialThresh; i++){
        	lowerHist[i] = histogram[i];
        }
        for(int i = initialThresh; i<histogram.length; i++){
        	upperHist[i-initialThresh] = histogram[i];
        }
        double newThresh = (Utils.histogramMean(lowerHist) + Utils.histogramMean(upperHist))/2;
        if(newThresh!=initialThresh){
        	return isoData((int)newThresh);
        } else return (int) newThresh;

    }
    
    public static void isoBinarize(int pixels[], int thresholdIso){
    	for(int i = 0; i < pixels.length; i++) {
    		int gray = ((pixels[i] & 0xff) + ((pixels[i] & 0xff00) >> 8) + ((pixels[i] & 0xff0000) >> 16)) / 3;
    		pixels[i] = gray < thresholdIso ? 0xff000000 : 0xffffffff;
    	}
    }

}
    
