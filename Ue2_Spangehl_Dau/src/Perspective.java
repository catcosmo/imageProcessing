// BV Ue2 WS2014/15 Vorgabe
//
// Copyright (C) 2013 by Klaus Jung

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class Perspective extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final String author = "Cat Spangehl, Julien Dau";		// TODO: type in your name here
	private static final String initialFilename = "59009_512.jpg";
	private static final File openPath = new File(".");
	private static final int maxWidth = 920;
	private static final int maxHeight = 920;
	private static final int border = 10;
	private static final double angleStepSize = 5.0;		// size used for angle increment and decrement
	private static final double strength= 0.001;
	private static final int[] white = new int[] {255, 255, 255};
	private static final int whitePixel = (0xFF<<24) | (255<<16) | (255<<8) | 255;

	private static JFrame frame;
	
	private ImageView srcView = null;		// source image view
	private ImageView dstView = null;		// rotated image view
	
	private JComboBox<String> methodList;	// the selected interpolation method
	private JSlider angleSlider;			// the selected angle
	private JLabel statusLine;				// to print some status text
	private double angle = 0.0;				// current angle in degrees
	/**
	 * Constructor. Constructs the layout of the GUI components and loads the initial image.
	 */
	public Perspective() {
        super(new BorderLayout(border, border));
        
        // load the default image
        File input = new File(initialFilename);
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        srcView = new ImageView(input);
        srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
        initDstView();
       
		// load image button
        JButton load = new JButton("Open Image");
        load.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		File input = openFile();
        		if(input != null) {
	        		srcView.loadImage(input);
	        		srcView.setMaxSize(new Dimension(maxWidth, maxHeight));
	        		initDstView();
	                calculatePerspective(false);
        		}
        	}        	
        });
         
        // selector for the rotation method
        String[] methodNames = {"Nearest Neighbour", "Bilinear Interpolation", "Bilinear Interpolation fast"};
        
        methodList = new JComboBox<String>(methodNames);
        methodList.setSelectedIndex(0);		// set initial method
        methodList.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                calculatePerspective(false);
        	}
        });
        
        // rotation angle minus button
        JButton decAngleButton = new JButton("-");
        decAngleButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		angle -= angleStepSize;
        		if(angle < 0) angle += 360;
        		angleSlider.setValue((int)angle);
                calculatePerspective(false);
        	}        	
        });
        
        // rotation angle plus button
        JButton incAngleButton = new JButton("+");
        incAngleButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		angle += angleStepSize;
        		if(angle > 360) angle -= 360;
        		angleSlider.setValue((int)angle);
                calculatePerspective(false);
        	}        	
        });
        
        // rotation angle slider
        angleSlider = new JSlider(0, 360, (int)angle);
        angleSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
        		angle = angleSlider.getValue();
                calculatePerspective(false);				
			}        	
        });
       
        // speed test button
        JButton speedTestButton = new JButton("Speed Test");
        speedTestButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		long startTime = System.currentTimeMillis();
        		double lastAngle = angle;
        		int cnt = 0;
        		for(angle = 0 ; angle < 360; angle += angleStepSize) {
        			calculatePerspective(true);
        			cnt++;
        		}
        		long time = System.currentTimeMillis() - startTime;
            	statusLine.setText("Speed Test: Calculated " + cnt + " perspcetives in " + time + " ms");
        		angle = lastAngle;
        	}        	
        });
        
        
        // some status text
        statusLine = new JLabel(" ");
        
        // arrange all controls
        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,border,0,0);
        controls.add(load, c);
        controls.add(methodList, c);
        controls.add(decAngleButton, c);
        controls.add(angleSlider, c);
        controls.add(incAngleButton, c);
        controls.add(speedTestButton, c);
        
        // arrange images
        JPanel images = new JPanel();
        images.add(srcView);
        images.add(dstView);
        
        // add to main panel
        add(controls, BorderLayout.NORTH);
        add(images, BorderLayout.CENTER);
        add(statusLine, BorderLayout.SOUTH);
               
        // add border to main panel
        setBorder(BorderFactory.createEmptyBorder(border,border,border,border));
        
        // perform the initial rotation
        calculatePerspective(false);
	}
	

	/**
	 * Set up and show the main frame.
	 */
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Perspective - " + author);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent contentPane = new Perspective();
        contentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(contentPane);

        // display the window
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
        frame.setVisible(true);
	}

	/**
	 * Main method. 
	 * @param args - ignored. No arguments are used by this application.
	 */
	public static void main(String[] args) {
        // schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	
	/**
	 * Open file dialog used to select a new image.
	 * @return The selected file object or null on cancel.
	 */
	private File openFile() {
		// file open dialog
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.jpg, *.png, *.gif)", "jpg", "png", "gif");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(openPath);
        int ret = chooser.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) return chooser.getSelectedFile();
        return null;		
	}
	
	/**
	 * Initialize the destination view giving it the correct size.
	 */
	private void initDstView() {
		// set destination size large enough to view a substantial part of the perspective
		
		int width = (int)(srcView.getImgWidth() * 1.4);
		int height = (int)(srcView.getImgHeight() * 1.2);
		
		// create an empty destination image
		if(dstView == null)
			dstView = new ImageView(width, height);
		else
			dstView.resetToSize(width, height);
		
		// limit viewing dimensions
		dstView.setMaxSize(new Dimension(maxWidth, maxHeight));
		
        frame.pack();
	}
    
	
    /**
     * Calculate image perspective and show result in destination view.
     * @param silent - set true when running the speed test (suppresses the image view).
     */
    protected void calculatePerspective(boolean silent) {
    	
    	if(!silent) {
    		// present some useful information
	    	statusLine.setText("Angle = " + angle + " degrees.");
    	}
    	
    	// get dimensions and pixels references of images
		int srcPixels[] = srcView.getPixels();
		int srcWidth = srcView.getImgWidth();
		int srcHeight = srcView.getImgHeight();
		int dstPixels[] = dstView.getPixels();
		int dstWidth = dstView.getImgWidth();
		int dstHeight = dstView.getImgHeight();

		long startTime = System.currentTimeMillis();
		
    	switch(methodList.getSelectedIndex()) {
    	case 0:	// Nearest Neigbour
    		calculateNearestNeigbour(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight, angle);
    		break;
    	case 1:	// Bilinear Interpolation
    		calculateBilinear(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight, angle);
    		break;
    	case 2:	// Bilinear Interpolation (faster [but less readable]!)
    		calculateBilinearFast(srcPixels, srcWidth, srcHeight, dstPixels, dstWidth, dstHeight, angle);
    		break;
    	}
    	
    	if(!silent) {
    		// show processing time
    		long time = System.currentTimeMillis() - startTime;
    		statusLine.setText("Angle = " + angle + " degrees. Processing time = " + time + " ms.");
    		// show the resulting image
     		dstView.applyChanges();
    	}
    }
    
    /**
     * Image perspective algorithm using nearest neighbour image rendering
     * @param srcPixels - source image pixel array of loaded image (ARGB values)
     * @param srcWidth - source image width
     * @param srcHeight - source image height
     * @param dstPixels - destination image pixel array to be filled (ARGB values)
     * @param dstWidth - destination image width
     * @param dstHeight - destination image height
     * @param degrees - angle in degrees for the perspective
     */
    void calculateNearestNeigbour(int srcPixels[], int srcWidth, int srcHeight, int dstPixels[], int dstWidth, int dstHeight, double degrees) {
		
    	for (int x = 0; x < dstWidth; ++x) {
        	for (int y = 0; y < dstHeight; ++y) {
        		
        		// Translate origin to the center of the image
        		double xTrans = translate(x, dstWidth);
        		double yTrans = translate(y, dstHeight);
        		
        		// Transform the destination image by calculating the source pixel positions based on black magic formulas
        		yTrans = ySrc(yTrans, strength, degrees);
        		xTrans = xSrc(xTrans, yTrans, strength, degrees);
        		
        		// Translate the origin back into the top-left corner
        		xTrans = translateBack(xTrans, srcWidth);
        		yTrans = translateBack(yTrans, srcHeight);
        		
        		// Round the values to get the nearest neighbour
        		int xSrc = (int) Math.round(xTrans);
        		int ySrc = (int) Math.round(yTrans);
        		
        		// Handle boundary issues by using white pixels when trying to access non-existing pixels
        		if (xSrc < 0 || ySrc < 0 || xSrc > (srcWidth-1) || ySrc > (srcHeight-1)) { 
        			dstPixels[Utils.pixelPos(x, y, dstWidth)] = Utils.writeBackPixels(white);
        		}
        		else {
        			dstPixels[Utils.pixelPos(x, y, dstWidth)] = srcPixels[Utils.pixelPos((int)xSrc, (int)ySrc, srcWidth)];
        		}      		
        		
        	}	
        	
    	}
    	
    }
 
    /**
     * Image perspective algorithm using bilinear interpolation
     * @param srcPixels - source image pixel array of loaded image (ARGB values)
     * @param srcWidth - source image width
     * @param srcHeight - source image height
     * @param dstPixels - destination image pixel array to be filled (ARGB values)
     * @param dstWidth - destination image width
     * @param dstHeight - destination image height
     * @param degrees - angle in degrees for the perspective
     */
    void calculateBilinear(int srcPixels[], int srcWidth, int srcHeight, int dstPixels[], int dstWidth, int dstHeight, double degrees) {
		
    	for (int x = 0; x < dstWidth; ++x) {
        	for (int y = 0; y < dstHeight; ++y) {
        		
        		// Translate origin to the center of the image
        		double xTrans = translate(x+0.5, dstWidth);
        		double yTrans = translate(y, dstHeight);
        		
        		// Transform the destination image by calculating the source pixel positions based on black magic formulas
        		yTrans = ySrc(yTrans, strength, degrees);
        		xTrans = xSrc(xTrans, yTrans, strength, degrees);
        		
        		// Translate the origin back into the top-left corner
        		xTrans = translateBack(xTrans, srcWidth);
        		yTrans = translateBack(yTrans, srcHeight);
        		
         		// Get the horizontal and vertical offset of the calculated source pixel coordinate (e.g: x=1.78 -> h=0.78)
        		double h = xTrans % 1;
        		double v = yTrans % 1;
        		
        		h = Math.abs(h);
        		v = Math.abs(v);
        		     		        		
        		// Loop from (x|y) to (x+1|y+1) (the four relevant pixels for interpolation)
        		int[][] RGBs = new int[4][3];
        		int xSrc;
        		int ySrc;
        		int pixelPos;
        		
        		for (int j=0; j<2; ++j) {
        			for (int i=0; i<2; ++i) {
        				xSrc = (xTrans<0) ? ((int)xTrans)-i : ((int)xTrans)+i;
        				ySrc = (yTrans<0) ? ((int)yTrans)-j : ((int)yTrans)+j;
        				pixelPos = Utils.pixelPos(i, j, 2);
        				
        				if (xSrc < 0 || ySrc < 0 || xSrc > srcWidth-1  || ySrc > srcHeight-1) {
        					RGBs[pixelPos] = white;
        				}
        				else {
        					RGBs[pixelPos] = Utils.pixel2RGB(srcPixels[Utils.pixelPos(xSrc, ySrc, srcWidth)]);
        				}
        			}
        		}
        		
        		// Alright, time to actually do the bilinear interpolation!
        		int[] interpolatedRGB = Utils.interpolate(RGBs[0], RGBs[1], RGBs[2], RGBs[3], h, v);
        		
        		// We're finally there, let's write our interpolated pixel to the destination image
        		dstPixels[Utils.pixelPos(x, y, dstWidth)] = Utils.RGB2pixel(interpolatedRGB);
        	}	
        	
    	}

    }
    
    /**
     * Image perspective algorithm using bilinear interpolation - hopefully quite fast
     * @param srcPixels - source image pixel array of loaded image (ARGB values)
     * @param srcWidth - source image width
     * @param srcHeight - source image height
     * @param dstPixels - destination image pixel array to be filled (ARGB values)
     * @param dstWidth - destination image width
     * @param dstHeight - destination image height
     * @param degrees - angle in degrees for the perspective
     */
    void calculateBilinearFast(int srcPixels[], int srcWidth, int srcHeight, int dstPixels[], int dstWidth, int dstHeight, double degrees) {
    	
    	double radians = Math.toRadians(degrees);
    	double cos     = Math.cos(radians);
    	double ssin    = strength * Math.sin(radians);
    	
    	int dstHeightHalf = (dstHeight>>1);
    	int dstWidthHalf  = (dstWidth>>1);
    	
    	int srcHeightHalf = (srcHeight>>1);
    	int srcWidthHalf  = (srcWidth>>1);
    	
    	double yOrigin, yTrans, ySrc, xOrigin, xTrans, xSrc, v, h;
    	int row, xSrcI, ySrcI, n;
    	int[][] RGBs;
    	
    	for (int yDst=0; yDst<dstHeight; ++yDst) {
    		yOrigin = yDst - dstHeightHalf;
    		yTrans  = (yOrigin / (cos - yOrigin * ssin));
    		ySrc    = yTrans + srcHeightHalf;
    		row     = yDst * dstWidth;
    		
    		if (ySrc < 0 || ySrc > srcHeight-1) {
    			
    			for (int xDst=0; xDst<dstWidth; ++xDst) {
    				dstPixels[row + xDst] = whitePixel;
    			}
    			
    		}
    		
    		else {
    			
    			v = ySrc % 1;
    		
	    		for (int xDst=0; xDst<dstWidth; ++xDst) {
	      			xOrigin = xDst - dstWidthHalf; 
	      			xTrans  = (xOrigin * (ssin * yTrans + 1));
	    			xSrc    = xTrans + srcWidthHalf;

	    			if (xSrc < -1 || xSrc > srcWidth) {
	    				dstPixels[row + xDst] = whitePixel;
	    				continue;
	    			}

	    			h = (xSrc<0) ? -xSrc % 1 : xSrc % 1;
	        		RGBs = new int[4][3];
	        		
	        		for (int j=0; j<2; ++j) {
	        			for (int i=0; i<2; ++i) {
	        				ySrcI = ((int)ySrc) + j;
	        				xSrcI = (xSrc<0) ? ((int)xSrc) - i : ((int)xSrc) + i;
	        				n = Utils.pixelPos(i, j, 2);
	        				        				
	        				if ((xSrcI < 0) || (ySrcI < 0) || (xSrcI > srcWidth-1) || (ySrcI > srcHeight-1)) {
	        					RGBs[n] = white;
	        				}
	        				else {
	        					RGBs[n] = Utils.pixel2RGB(srcPixels[Utils.pixelPos(xSrcI, ySrcI, srcWidth)]);
	        				}
	        			}
	        		}
	        			        		
	        		dstPixels[row + xDst] = Utils.RGB2pixel(Utils.interpolate(RGBs[0], RGBs[1], RGBs[2], RGBs[3], h, v));
	        	}
    		}
    	}
    }
 
    private double xSrc(double xDst, double ySrc, double strength, double degrees) {
    	return xDst * (strength*Math.sin(Math.toRadians(degrees))*ySrc + 1);
    }
    
    private double ySrc(double yDst, double strength, double degrees) {
    	return yDst / (Math.cos(Math.toRadians(degrees)) - yDst*strength*Math.sin(Math.toRadians(degrees)));
    }
    
    private double translate(int n, int size) {
    	return n - (((double)size)/2.0);
    }
    
    private double translate(double n, int size) {
    	return n - (((double)size)/2.0);
    }
    
    private double translateBack(double n, int size) {
    	return n + (((double)size)/2.0);
    }
    
}

