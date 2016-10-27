// BV Ue05 WS2014/15 Vorgabe Hilfsklasse StatsView
//
// Copyright (C) 2014 by Klaus Jung

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class StatsView extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String[] names = { "Minimum:", "Maximum:", "Mean:", "Median:", "Varianz:", "Entropie:" }; // TODO: enter proper names
	private static final int rows = names.length;
	private static final int border = 2;
	private static final int columns = 2;
	private static final int graySteps = 256;
	
	private JLabel[] infoLabel = new JLabel[rows];
	private JLabel[] valueLabel = new JLabel[rows];
	
	private int[] histogram = null;
	
	public StatsView() {
		super(new GridLayout(rows, columns, border, border));
		TitledBorder titBorder = BorderFactory.createTitledBorder("Statistics");
		titBorder.setTitleColor(Color.GRAY);
		setBorder(titBorder);
		for(int i = 0; i < rows; i++) {
			infoLabel[i] = new JLabel(names[i]);
			valueLabel[i] = new JLabel("-----");
			add(infoLabel[i]);
			add(valueLabel[i]);
		}
	}
	
	private void setValue(int column, int value) {
		valueLabel[column].setText("" + value);
	}
	
	private void setValue(int column, double value) {
		valueLabel[column].setText(String.format(Locale.US, "%.2f", value));
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

		// TODO: calculate and display statistic values
		int sum         = Utils.histogramTotal(histogram);
		int max         = Utils.histogramMax(histogram);
		int min         = Utils.histogramMin(histogram);
		double mean     = Utils.histogramMean(histogram, sum);
		int median      = Utils.histogramMedian(histogram, sum);
		double variance = Utils.histogramVariance(histogram, mean, sum);
		double entropy  = Utils.histogramEntropy(histogram, sum);

		setValue(0, min);
		setValue(1, max);
		setValue(2, mean);
		setValue(3, median);
		setValue(4, variance);
		setValue(5, entropy);
		
		return true;
	}

}
