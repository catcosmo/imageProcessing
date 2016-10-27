// BV Ue05 WS2014/15 Vorgabe
//
// Copyright (C) 2014 by Klaus Jung

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.CSS;

import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class ImageAnalysis extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private static final String author = "Cat Spangehl, Julien Dau";
	private static final String initialFilename = "mountains.png";
	private static final File openPath = new File(".");
	private static final int border = 10;
	private static final int maxWidth = 920;
	private static final int maxHeight = 920;
	private static final int graySteps = 256;
	
	private static JFrame frame;
	
	private ImageView imgView;						// image view
	private HistoView histoView = new HistoView();	// histogram view
	private StatsView statsView = new StatsView();	// statistics values view
	private JSlider brightnessSlider;				// brightness Slider
	
	// TODO: add an array to hold the histogram of the loaded image
	private int[] histogram;
	
	// TODO: add an array that holds the ARGB-Pixels of the originally loaded image
	private int[] origPixels;
	
	// TODO: add a contrast slider
	private JSlider contrastSlider;					// contrast Slider
	
	private int brightness = 0;
	private float contrast = 1f;
	
	private JLabel statusLine;				// to print some status text
	
	/**
	 * Constructor. Constructs the layout of the GUI components and loads the initial image.
	 */
	public ImageAnalysis() {
        super(new BorderLayout(border, border));
        
        // load the default image
        File input = new File(initialFilename);
        
        if(!input.canRead()) input = openFile(); // file not found, choose another image
        
        imgView = new ImageView(input);
        imgView.setMaxSize(new Dimension(maxWidth, maxHeight));
        
        // TODO: set the histogram array of histView and statsView
        histogram = new int[256];
        
        // TODO: initialize the original ARGB-Pixel array from the loaded image
        origPixels = imgView.getPixels().clone();
       
		// load image button
        JButton load = new JButton("Open Image");
        load.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		File input = openFile();
        		if(input != null) {
        			imgView.loadImage(input);
        			imgView.setMaxSize(new Dimension(maxWidth, maxHeight));
        			
        	        // TODO: initialize the original ARGB-Pixel array from the newly loaded image
        			origPixels = imgView.getPixels().clone();
        			
        			frame.pack();
	                processImage();
        		}
        	}        	
        });
         
        JButton reset = new JButton("Reset Slider");
        reset.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		brightnessSlider.setValue(0);

        		// TODO: reset contrast slider
        		contrastSlider.setValue(100);
        		
        		processImage();
	    	}        	
	    });
        
        // some status text
        statusLine = new JLabel(" ");
        
        // top view controls
        JPanel topControls = new JPanel(new GridBagLayout());
        topControls.add(load);
        topControls.add(reset);
        
        // center view
        JPanel centerControls = new JPanel();
        JPanel rightControls = new JPanel();
        rightControls.setLayout(new BoxLayout(rightControls, BoxLayout.Y_AXIS));
        centerControls.add(imgView);
        rightControls.add(histoView);
        rightControls.add(statsView);
        centerControls.add(rightControls);
        
        // bottom view controls
        JPanel botControls = new JPanel();
        botControls.setLayout(new BoxLayout(botControls, BoxLayout.Y_AXIS));
        
        // brightness slider
        brightnessSlider = new JSlider(-graySteps, graySteps, brightness);
		TitledBorder brightnessSliderBorder = BorderFactory.createTitledBorder("Brightness: "+String.valueOf(brightness));
		brightnessSliderBorder.setTitleColor(Color.GRAY);
        brightnessSlider.setBorder(brightnessSliderBorder);
        brightnessSlider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {

        		brightness = brightnessSlider.getValue();	// Holt Wert des Helligkeitssliders (Werte: -256..+256)
        		TitledBorder tb = (TitledBorder) ((JSlider)e.getSource()).getBorder();
        		tb.setTitle("Brightness: "+String.valueOf(brightness));
        		processImage();
        	}        	
        });
        
        // TODO: setup contrast slider
        contrastSlider = new JSlider(0, 1000, 100);
		TitledBorder contrastSliderBorder = BorderFactory.createTitledBorder("Contrast: "+String.valueOf(contrast));
		contrastSliderBorder.setTitleColor(Color.GRAY);
        contrastSlider.setBorder(contrastSliderBorder);
        contrastSlider.addChangeListener(new ChangeListener() {
        	public void stateChanged(ChangeEvent e) {

        		contrast = (float) contrastSlider.getValue() / 100f;	// Holt Wert des Kontrastsliders (Werte: 0..1000); Werte sollen von 0 bis 10 gehen, deshalb durch 100 teilen
        		TitledBorder tb = (TitledBorder) ((JSlider)e.getSource()).getBorder();
        		tb.setTitle("Contrast: "+String.valueOf(contrast));
        		processImage();
        	}        	
        });
        
        botControls.add(brightnessSlider);
        botControls.add(contrastSlider); // TODO: add contrast slider
        statusLine.setAlignmentX(Component.CENTER_ALIGNMENT);
        botControls.add(statusLine);

        // add to main panel
        add(topControls, BorderLayout.NORTH);
        add(centerControls, BorderLayout.CENTER);
        add(botControls, BorderLayout.SOUTH);
               
        // add border to main panel
        setBorder(BorderFactory.createEmptyBorder(border,border,border,border));
        
        // perform the initial rotation
        processImage();
	}
	
	/**
	 * Set up and show the main frame.
	 */
	private static void createAndShowGUI() {
		// create and setup the window
		frame = new JFrame("Image Analysis - " + author);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JComponent contentPane = new ImageAnalysis();
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
     * Update image with new brightness and contrast values.
     * Update histogram, histogram view and statistics view.
     */
    protected void processImage() {    	
		long startTime = System.currentTimeMillis();
		
		// TODO: add your processing code here
		histogram = new int[256];										// reset histogram, otherwise we just continue to add to the values over and over again
		int[] modPixels = new int[origPixels.length];					// an array for the modified pixels
		
    	for (int i=0; i<origPixels.length; ++i) { 						// loop over all pixels
    		
    		int[] rgb = Utils.pixel2RGB(origPixels[i]); 				// get RGB values
    		double[] YCbCr = Utils.RGB2YCbCr(rgb[0], rgb[1], rgb[2]); 	// neues array mit YCbCr values fuer helligkeit+kontrast berechnung
    		
    		YCbCr = Utils.changeBrightness(YCbCr, brightness);  		// berechnung neue brighntness
    		YCbCr = Utils.changeContrast(YCbCr, contrast);				// berechnung neuer contrast
    		
    		rgb = Utils.YCbCr2RGB(YCbCr[0], YCbCr[1], YCbCr[2]);  		// rueckrechnung auf rgb
    		rgb = Utils.capValues(rgb); 								// sicherstellen, dass werte im bereich 0-255 liegen
    		
			int greyValue = (rgb[0] + rgb[1] + rgb[2]) / 3; 			// convert to greyscale (histogram doesn't care about colors [yet])
    		++histogram[greyValue]; 									// histogramm wird an stelle <greyValue> um 1 erhoeht
    		
    		modPixels[i] = Utils.RGB2pixel(rgb);
    	}

    	histoView.setHistogram(histogram); 								// histogram[] an histoView uebergeben
    	statsView.setHistogram(histogram); 								// histogram[] an statsView uebergeben
    	
    	imgView.setPixels(modPixels);									// bearbeitete pixel an imgView uebergeben
		imgView.applyChanges();
		histoView.update();
		statsView.update();
		
		// show processing time
		long time = System.currentTimeMillis() - startTime;
		statusLine.setText("Processing time = " + time + " ms.");
    }
    
}

