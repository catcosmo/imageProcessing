// BV Ue05 WS2014/15 Vorgabe Hilfsklasse HistoView
//
// Copyright (C) 2014 by Klaus Jung

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class HistoView extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int graySteps = 256;
	private static final int height = 200;
	private static final int width = graySteps;
	
	private int[] histogram = null;
	
	public HistoView() {
		super();
		TitledBorder titBorder = BorderFactory.createTitledBorder("Histogram");
		titBorder.setTitleColor(Color.GRAY);
		setBorder(titBorder);
		add(new HistoScreen());
	}
	
	public boolean setHistogram(int[] histogram) {
		if(histogram == null || histogram.length != graySteps) {
			return false;
		}
		this.histogram = histogram;
		update();
		return true;
	}
	
	public boolean update() {
		if(histogram == null) {
			return false;
		}
		invalidate();
		repaint();
		return true;
	}
	
	class HistoScreen extends JComponent {

		private static final long serialVersionUID = 1L;
		
		public void paintComponent(Graphics g) {
			g.clearRect(0, 0, width, height);
			g.setColor(Color.black);
			
			// TODO: draw histogram instead of diagonal lines
			double scaleFactor = 200.0 / Utils.maxVal(histogram);	// Faktor, mit dem jede Linie skaliert wird; basiert auf groesstem Wert des Histogramms
			
			for (int x=0; x<histogram.length; ++x) {
				int value = (int) ((float) histogram[x] * scaleFactor);	// Hoehe des histograms mit Skalierungsfaktor multiplizieren; float-cast weil sonst beim Teilen (fast) immer auf 0 gerundet wuerde
				g.drawLine(x, 200, x, 200-(value));						// 200: Um unten mit dem Zeichnen zu beginnen
			}
			
		}
		
		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}
	}
	


}
