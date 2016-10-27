
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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
 
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;
 
public class FloodFilling02 extends JPanel {
 
    private static final long serialVersionUID = 1L;
    private static final int border = 10;
    private static final int maxWidth = 400;
    private static final int maxHeight = 400;
    private static final int BLACK = Utils.ARGB_BLACK;
    private static final int WHITE = Utils.ARGB_WHITE;
    private static final File openPath = new File(".");
    private static final String title = "Binarisierung";
    private static final String author = "Spangehl";
    private static final String initalOpen = "tools1.png";
    private static int memorySize = 0;
 
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
    private static JCheckBox outlineBox = new JCheckBox("Outline");
    private static int threshold = 128; // value for slider
 
    public FloodFilling02() {
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
 
        JComponent newContentPane = new FloodFilling02();
        newContentPane.setOpaque(true); // content panes must be opaque
        frame.setContentPane(newContentPane);
 
        // display the window.
        frame.pack();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        frame.setLocation((screenSize.width- frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
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
        String stackQueue ="";
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
            // seqRegion();
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
            statusLine.setText(text + ". The maximum size of the "+stackQueue +" was " + memorySize + ".");
        memorySize = 0;
    }
 
    //TODO randomColors auslagern (für alle 4)
    private void breadthFirstEvolved(int[] pixels) {
        boolean[] visited = new boolean[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] == BLACK) {
                int r = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int g = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int b = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int rgb = 65536 * r + 256 * g + b;
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
            int[] neighbourhood = getNeighboursInPicture(pixels, currentPos, 9, visited);
            for (int j = 0; j < neighbourhood.length; j++) {
                if (neighbourhood[j] != 0 && visited[neighbourhood[j]] == false) {
                    visited[neighbourhood[j]] = true;
                    queue.offer(getPixelPos(neighbourhood[j], srcView.getImgWidth()));
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
                int r = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int g = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int b = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int rgb = 65536 * r + 256 * g + b;
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
                int r = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int g = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int b = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int rgb = 65536 * r + 256 * g + b;
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
            int[] neighbourhood = getNeighbours(pixels, currentPos, 9);
            for (int j = 0; j < neighbourhood.length; j++) {
                if (neighbourhood[j] != 0 && visited[neighbourhood[j]] == false) {
                    visited[neighbourhood[j]] = true;
                    stack.push(getPixelPos(neighbourhood[j], srcView.getImgWidth()));
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
            int[] neighbourhood = getNeighboursInPicture(pixels, currentPos, 9, visited);
            for (int j = 0; j < neighbourhood.length; j++) {
                if (neighbourhood[j] != 0 && visited[neighbourhood[j]] == false) {
                    visited[neighbourhood[j]] = true;
                    stack.push(getPixelPos(neighbourhood[j], srcView.getImgWidth()));
                }
            }
        }
        if (memorySize < stack.size())
            memorySize = stack.size();
    }
    
    
    //TODO: dynamisieern
    private int[] getNeighbours(int[] pixels, int pixelPos, int kernelSize) {
        int[] neighbours = new int[kernelSize];
        int neighbourcount = 0;
        int[] xY = getPixelPos(pixelPos, srcView.getImgWidth());
        int x = xY[0];
        int y = xY[1];
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int currWidth = x + j;
                int currHeight = y + i;
                neighbours[neighbourcount] = Utils.pixelPosSafe(currWidth, currHeight, srcView.getImgWidth(),
                        srcView.getImgHeight());
                neighbourcount++;
            }
        }
        return neighbours;
    }
 
    private int[] getNeighboursInPicture(int[] pixels, int pixelPos, int kernelSize, boolean[] visited) {
        int[] neighbours = new int[kernelSize];
        int neighbourcount = 0;
        int[] xY = getPixelPos(pixelPos, srcView.getImgWidth());
        int x = xY[0];
        int y = xY[1];
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int currWidth = x + j;
                int currHeight = y + i;
                if (currWidth > 0 && currHeight > 0 && !(currWidth == x && currHeight == y)
                        && currWidth < srcView.getWidth() && currHeight < srcView.getHeight()) {
                    int thisPixelPos = Utils.pixelPosSafe(currWidth, currHeight, srcView.getImgWidth(),
                            srcView.getImgHeight());
                    if (pixels[thisPixelPos] == BLACK && visited[thisPixelPos] == false) {
                        neighbours[neighbourcount] = Utils.pixelPosSafe(currWidth, currHeight, srcView.getImgWidth(),
                                srcView.getImgHeight());
                        neighbourcount++;
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
                int r = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int g = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int b = ThreadLocalRandom.current().nextInt(1, 255 + 1);
                int rgb = 65536 * r + 256 * g + b;
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
            int[] neighbourhood = getNeighbours(pixels, currentPos, 9);
            for (int j = 0; j < neighbourhood.length; j++) {
                if (neighbourhood[j] != 0 && visited[neighbourhood[j]] == false) {
                    visited[neighbourhood[j]] = true;
                    queue.offer(getPixelPos(neighbourhood[j], srcView.getImgWidth()));
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
        System.out.println(xY[0] + "+" + xY[1]);
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