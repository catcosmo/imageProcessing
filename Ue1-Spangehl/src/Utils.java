
public class Utils {
	
	public static final int ARGB_WHITE = 0xffffffff;
	public static final int ARGB_BLACK = 0xff000000;
	public static final int ARGB_RED   = 0xffff0000;
	public static final int ARGB_GREEN = 0xff00ff00;
	public static final int ARGB_BLUE  = 0xff0000ff;

	/**
	 * Extract the RGB color information from a single int into an array.
	 * @param argb The int holding the ARGB color information
	 * @return An array where index 0 hold the red channel, index 1 the green channel and index 2 the blue channel
	 */
	public static int[] fetchRGBValues(int argb) {
		return new int[] {((argb>>16) & 0xff), ((argb>>8) & 0xff), (argb & 0xff)};
	}
	
	/*
	 *	Alias for fetchRGBValues
	 */
	public static int[] pixel2RGB(int argb) {
		return Utils.fetchRGBValues(argb);
	}
	
	/**
	 * Turn an array with separated RGB color values into a single int holding the same information.
	 * @param RGB An array where index 0 hold the red channel, index 1 the green channel and index 2 the blue channel
	 * @return An int holding the ARGB color information
	 */
	public static int writeBackPixels(int[] RGB) {
		return (0xFF<<24) | (RGB[0]<<16) | (RGB[1]<<8) | RGB[2];
	}
	
	/**
	 * Turn a simple greyscale color value (0..255) into an ARGB int holding the same information.
	 * @param greyValue A grey value from the range 0..255 (black to white)
	 * @return An int holding the (all grey) ARGB color information
	 */
	public static int writeBackGreyPixels(int greyValue) {
		return (0xFF<<24) | (greyValue<<16) | (greyValue<<8) | greyValue;
	}
	
	/*
	 *	Alias for writeBackPixels 
	 */
	public static int RGB2pixel(int[] rgb) {
		return Utils.writeBackPixels(rgb);
	}
	
	/**
	 * Calculates the index of an image's pixel in a one-dimensional representation.
	 * @param x The pixel's x position (horizontal - the column the pixel is in)
	 * @param y The pixel's y position (vertical - the row the pixel is in)
	 * @param width The image's total width in pixels
	 * @return The pixel's index in the image's one-dimensional representation
	 */
	public static int pixelPos(int x, int y, int width) {
		return (y * width + x);
	}
	
	/**
	 * Calculates the index of an image's pixel in a one-dimensional representation.
	 * In comparison to pixelPos, this method will make sure to only return pixel positions
	 * that are actually within the bounds of the given image dimensions.
	 * @param x The pixel's x position (horizontal - the column the pixel is in)
	 * @param y The pixel's y position (vertical - the row the pixel is in)
	 * @param width The image's total width in pixels
	 * @param height The image's total height in pixels
	 * @return The pixel's index in the image's one-dimensional representation
	 */
	public static int pixelPosSafe(int x, int y, int width, int height) {
		x = (x < 0) ? 0 : x;
		y = (y < 0) ? 0 : y;
		x = (x > width-1)  ? width-1  : x;
		y = (y > height-1) ? height-1 : y;
		return (y * width + x);
	}
	
	/**
	 * Makes sure the int values in the provided array are in the range 0..255
	 * @param rgb An array with int values representing RGB colors
	 * @return The input array with all values in the range 0..255
	 */
	public static int[] capValues(int[] rgb) {
		return new int[] { Utils.capValue(rgb[0]), Utils.capValue(rgb[1]), Utils.capValue(rgb[2]) };
	}

	/**
	 * Makes sure the provided int value is in the range 0..255
	 * @param n An int value representing a color
	 * @return The input value, capped to the range 0..255
	 */
	public static int capValue(int n) {
		n = (n<0)   ? 0   : n;
		n = (n>255) ? 255 : n;
		return n;
	}
	
	/**
	 * Extract a square subset of pixels from an image, provided as array of ARGB pixels.
	 * For non-existing pixels at the edges of the image, the closest existing pixel will be used.
	 * @param pixels	One-dimensional array of ARGB pixels, representing a 2D image
	 * @param width		The width (in pixels) of the image represented by pixels
	 * @param height	The height (in pixels) of the image represented by pixels
	 * @param size		The size (in pixels) of the kernel to be extracted
	 * @param x			The horizontal position for the kernel
	 * @param y			The vertical position for the kernel
	 * @return			The kernel as one-dimensional array of ARGB pixels
	 */
	public static int[] getKernel(int[] pixels, int width, int height, int size, int x, int y) {
		int r = (size-1)/2;
		int[] kernel = new int[size*size];
		
		int xStart = x-r;
		int yStart = y-r;
		
		int curPosX;
		int curPosY;
		
		for(int j=0; j<size; j++) { // Walk the rows (y)
			curPosY = yStart + j;
			// Prevent boundary issues
			if (curPosY < 0) { curPosY = 0; }
			if (curPosY >= height) { curPosY = height-1; }
			
			for(int i=0; i<size; i++){ // Walk the columns (x)
				curPosX = xStart + i;
				// Prevent boundary issues
				if (curPosX < 0) { curPosX = 0;}
				if (curPosX >= width) { curPosX = width-1; }
				
				kernel[Utils.pixelPos(i, j, size)] = pixels[Utils.pixelPos(curPosX, curPosY, width)];
			}
		}
		
		return kernel;
	}
	
	/**
	 * Sets all pixels to white
	 * @param pixels An array of argb pixels
	 */
	public static void initWhite(int[] pixels) {
		for (int i=0; i<pixels.length; ++i) {
			pixels[i] = Utils.ARGB_WHITE;
		}
	}
	
	/**
	 * Inverts the color of all pixels in the given argb pixel array
	 * @param pixels An array of argb pixels
	 */
	public static void invertImage(int[] pixels) {
		for (int i=0; i<pixels.length; ++i) {
			int[] rgb = Utils.pixel2RGB(pixels[i]);
			rgb[0] = 255-rgb[0];
			rgb[1] = 255-rgb[1];
			rgb[2] = 255-rgb[2];
			pixels[i] = Utils.RGB2pixel(rgb);
		}
	}
	
	public static void dilate(int[] srcPixels, int[] dstPixels, int width, int height, float radius, int x, int y) {
		int size = ((int) radius) * 2 + 1;
		
		int xStart = x - (int) radius;
		int yStart = y - (int) radius;
		
		int curPosX;
		int curPosY;
		
		int pixel;
		// prevent boundary issues by using a white pixel in case we're outside of the image
		if (x < 0 || y < 0 || y >= height || x >= width) {
			pixel = Utils.ARGB_WHITE;
		}
		else {
			pixel = srcPixels[Utils.pixelPos(x, y, width)];
		}
		
		// in case the pixel is in fact black...
		if (pixel == Utils.ARGB_BLACK) {
			
			for(int j=0; j<size; j++) { // Walk the rows (y)
				curPosY = yStart + j;
				
				for(int i=0; i<size; i++){ // Walk the columns (x)
					curPosX = xStart + i;
				
					double distance = distance(x, y, curPosX, curPosY);
					if (distance <= radius) {
						if (curPosX >= 0 && curPosY >= 0 && curPosY < height && curPosX < width) {
							dstPixels[Utils.pixelPos(curPosX, curPosY, width)] = Utils.ARGB_BLACK;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Calculates ans returns the distance between two points.
	 * @param x1 The first point's x coordinate
	 * @param y1 The first point's y coordinate
	 * @param x2 The second point's x coordinate
	 * @param y2 The second point's y coordinate
	 * @return The distance between the given points
	 */
	public static double distance(int x1, int y1, int x2, int y2) {
		int deltaX = x1-x2;
		int deltaY = y1-y2;
		return Math.sqrt((deltaX*deltaX + deltaY*deltaY));
	}
	
	/**
	 * Takes an array of int values representing argb pixel and turns
	 * them into black or white depending on the given threshold.
	 * @param pixels An array of argb pixels
	 * @param threshold The greyscale value (0..255) for which the pixels will be set to black
	 * @return The pixel array with only black and white pixels
	 */
	public static int[] makeBW(int[] pixels, int threshold) {	
		for (int i=0; i<pixels.length; ++i) {
			int[] RGB = Utils.fetchRGBValues(pixels[i]);
			RGB[0] = RGB[1] = RGB[2] = (RGB[0] + RGB[1] + RGB[2]) / 3;
			RGB[0] = RGB[1] = RGB[2] = (RGB[0] <= threshold) ? 0 : 255;
			pixels[i] = Utils.writeBackPixels(RGB);
		}
		return pixels;
	}
	
	/**
	 * Takes an array of int values representing argb pixel and turns them into greyscale.
	 * @param pixels An array of argb pixels
	 * @return The pixel array with all greyscale pixels
	 */
	public static int[] makeGrey(int[] pixels) {
		for (int i=0; i<pixels.length; ++i) {
			int[] RGB = Utils.fetchRGBValues(pixels[i]);
			RGB[0] = RGB[1] = RGB[2] = (RGB[0] + RGB[1] + RGB[2]) / 3;
			pixels[i] = Utils.writeBackPixels(RGB);
		}
		return pixels;
	}
	
	/**
	 * Returns the smallest numerical value from the given array
	 * @param values An array of int values
	 * @return The smallest int value from within the array
	 */
	public static int minVal(int[] values) {
		int min = values[0];
		for (int i=1; i<values.length; ++i) {
			if (values[i] < min) {
				min = values[i];
			}
		}
		return min;
	}
	
	/**
	 * Returns the biggest numerical value from the given array
	 * @param values An array of int values
	 * @return The biggest int value from within the array
	 */
	public static int maxVal(int[] values) {
		int max = values[0];
		for (int i=1; i<values.length; ++i) {
			if (values[i] > max) {
				max = values[i];
			}
		}
		return max;
	}
	
	/**
	 * Finds the biggest numerical value in the array and returns the array's index position where this value can be found
	 * @param values An array of int values
	 * @return The array index where the biggest value in the array can be found
	 */
	public static int maxValIndex(int[] values) {
		int max = values[0];
		int maxIndex = 0;
		for (int i=1; i<values.length; ++i) {
			if (values[i] > max) {
				max = values[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	/**
	 * Finds the smallest numerical value in the array and returns the array's index position where this value can be found
	 * In case the smallest value occurs several times, the latest (greatest) index holding that value will be returned
	 * @param values An array of int values
	 * @return The array index where the smallest value in the array can be found
	 */
	public static int minValIndex(int[] values) {
		int min = values[0];
		int minIndex = 0;
		for (int i=1; i<values.length; ++i) {
			if (values[i] <= min) {
				min = values[i];
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	/**
	 * Counts and returns the total number of pixels (or rather: the values representing the number of pixels) in this histogram
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @return The total number of pixels that has been counted with this histogram
	 */
	public static int histogramTotal(int[] histogram) {
		int count = 0;
		double mean = 0;
		for (int i=0; i<histogram.length; ++i) {
			count += histogram[i];
		}
		return count;
	}
	
	/**
	 * Finds the first and "smallest" color value (= array index) with a value (= pixel count) greater than zero within the given histogram.
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @return The "smallest" color value with a pixel count of at least one or -1 if the histogram is empty
	 */
	public static int histogramMin(int[] histogram) {
		for (int i=0; i<histogram.length; ++i) {
			if (histogram[i] > 0) {
				return i;
			} 
		} return -1; // Keine Pixel im Bild bzw. Werte im Histogram! 
	}
	
	/**
	 * Finds the last and "biggest" color value (= array index) with a value (= pixel count) greater than zero within the given histogram.
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @return The "biggest" color value with a pixel count of at least one or -1 if the histogram is empty
	 */
	public static int histogramMax(int[] histogram) {
		for (int i=histogram.length-1; i>=0; i--) {
			if (histogram[i] > 0) {
				return i;
			} 
		} return -1; // Keine Pixel im Bild bzw. Werte im Histogram! 
	}
	
	/**
	 * Calculates and returns the histogram's mean value
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @return the histogram's mean value
	 */
	public static double histogramMean(int[] histogram) {
		int pixelCount = Utils.histogramTotal(histogram);
		return Utils.histogramMean(histogram, pixelCount);
	}
	
	public static double histogramMean(int[] histogram, int total) {
		double mean = 0.0;
		for (int i=0; i<histogram.length; ++i) {
			mean += histogram[i] * i;
		}
		return (mean / total);
	}
	
	/**
	 * Calculates and returns the histogram's median value
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @return the histogram's median value
	 */
	public static int histogramMedian(int[] histogram) {
		int pixelCount = Utils.histogramTotal(histogram);
		return histogramMedian(histogram, pixelCount);
	}

	/**
	 * Calculates and returns the histogram's median value
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @param total The sum of all values in the given histogram
	 * @return the histogram's median value
	 */
	public static int histogramMedian(int[] histogram, int total) {
		int median = 0;							// Der zu berechnende Median (Grauwert im Histogram)
		int counter = 0;						// Aktueller Laufwert (wieviele Pixel des Histogram habe ich bereits abgelaufen?)
		int half = (int) ((float) total * 0.5);	// Haelfte der Anzahl an Pixeln im Histogram (markiert damit die Medianposition vom Histogram)
		for (int i=0; i<histogram.length; ++i) {
			counter += histogram[i];			
			if (half <= counter) {				// Median (Haelfte der Pixel im Histogram) ist erreicht!
				median = i;						// Aktuellen Grauwert merken, den wollen wir zurueckgeben
				break;
			}
		}
		return median;
	}
	
	/**
	 * Calculates and returns the histogram's variance
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @return the histogram's variance
	 */
	public static double histogramVariance(int[] histogram) {
		double mean = Utils.histogramMean(histogram);
		int pixelCount = Utils.histogramTotal(histogram);
		return histogramVariance(histogram, mean, pixelCount);
	}
	
	/**
	 * Calculates and returns the histogram's variance
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @param mean The given histogram's mean value
	 * @param total The sum of all values in the given histogram
	 * @return
	 */
	public static double histogramVariance(int[] histogram, double mean, int total) {
		double variance = 0.0;
		for (int i=0; i<histogram.length; ++i) {
			for (int j=0; j < histogram[i]; ++j) {		// Die beiden Schleifen gucken sich jedes einzelne Pixel/Farbwert an
				variance += (i - mean) * (i - mean);	// Berechnen der Varianz (Abweichung zum Quadrat normiert)
			}
		}
		return (variance / total);
	}
	
	/**
	 * Calculates and returns the histogram's entropy
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @return the histogram's entropy
	 */
	public static double histogramEntropy(int[] histogram) {
		int pixelCount = Utils.histogramTotal(histogram);
		return histogramEntropy(histogram, pixelCount);
	}

	/**
	 * 
	 * @param histogram A histogram, usually with 256 elements, each representing a color value and the number of pixels with that color
	 * @param total The sum of all values in the given histogram
	 * @return
	 */
	public static double histogramEntropy(int[] histogram, int total) {
		double entropy = 0.0;
		for (int i = 0; i<histogram.length; ++i) {
			double p = (float) histogram[i] / total;			// Wahrscheinlichkeit des "Ereignisses" Farbwert i
			if (p > 0) {										// log(0)=NaN, braucht man fuer die Berechnung aber auch nicht
				entropy += (p * -(Math.log(p) / Math.log(2)));	// Summenformel Entropie
			}
		}
		return entropy;
	}	

	/**
	 * Converts RGB to YCbCr color values
	 * @param r The pixel's red channel value (range 0..255)
	 * @param g The pixel's green channel value (range 0..255)
	 * @param b The pixel's blue channel value (range 0..255)
	 * @return The given RGB color array converted to the YCbCr color spectrum
	 */
    public static double[] RGB2YCbCr(int r, int g, int b) {
    	double[] YCbCr = new double[3];
    	YCbCr[0] = (0.299*r)+(0.587*g)+(0.114*b);
    	YCbCr[1] = -(0.168736*r)-(0.331264*g)+(0.5*b);
    	YCbCr[2] = (0.5*r)-(0.418688*g)-(0.081312*b);
    	return YCbCr;
    }
    
    /**
     * Converts YCbCr to RGB color values
     * @param Y The pixel's base luminance channel value
     * @param Cb The pixel's blue-yellow chrominance channel value
     * @param Cr The pixel's red-green chrominance channel value
     * @return The given YCbCr color array converted to the RGB color spectrum
     */
    public static int[] YCbCr2RGB(double Y, double Cb, double Cr) {
    	int[] RGB = new int[3];
    	RGB[0] = (int) Math.round(Y+(1.402*Cr));
    	RGB[1] = (int) Math.round(Y-(0.3441*Cb)-(0.7141*Cr));
    	RGB[2] = (int) Math.round(Y+(1.772*Cb));
    	return RGB;
    }
	
    /**
     * Change the given pixel's brightness
     * @param YCbCr The pixel's color values as YCbCr array
     * @param brightness The brightness modification (-/+)
     * @return The brighter/darker pixel as YCbCr array
     */
	public static double[] changeBrightness(double[] YCbCr, int brightness) {
		YCbCr[0] = YCbCr[0] + brightness;
		return YCbCr;
	}
	
	/**
	 * Change the given pixel's saturation
	 * @param YCbCr The pixel's color values as YCbCr array
	 * @param saturation The saturation factor
	 * @return The more/less saturated pixel as YCbCr array
	 */
	public static double[] changeSaturation(double[] YCbCr, double saturation) {
		YCbCr[1] = YCbCr[1] * saturation;
		YCbCr[2] = YCbCr[2] * saturation;
		return YCbCr;
	}
	
	/**
	 * Change the given pixel's contrast
	 * @param YCbCr The pixel's color values as YCbCr array
	 * @param contrast The contrast factor (recommended range: 0..10)
	 * @return The brighter/darker pixel as YCbCr array
	 */
	public static double[] changeContrast(double[] YCbCr, double contrast) {
		YCbCr[0] = ((YCbCr[0] - 127.5) * contrast) + 127.5;
		return YCbCr;
	}
	
	// TODO autocorrect, korrigiert leider nur das histogram, aendert aber nicht das bild :)
	public static void autoCorrectHisto(int[] histogram, int min, int max){
		int newSteps = -(min-max)/histogram.length; 				//errechenet abstand neuer balken
		for(int i=0; i <histogram.length; i++){						//laeuft durchs histogram
			histogram[i] = histogram[min];							//wert an der stelle null(erster durchlauf) bzw i wird mit dem minimalwert gefuellt
			i += newSteps;											//zum naechsten zu zeichnenden balken springen
			min += 1;												//zum nächsten wert springen
		}
	}
	
	// TODO vernünftiges autokorrekt, leider noch nicht ganz richtig
	public static void autoCorrect(int[] pixels, int[] histogram, int min, int max){		
		int newSteps = -(min-max)/histogram.length; 				//errechenet abstand neuer balken

		for(int i=0; i<pixels.length; ++i) { 							//fuer jeden pixel im bild
			
			int[] rgb = Utils.pixel2RGB(pixels[i]); 					//get RGB values
    		int value = (rgb[0] + rgb[1] + rgb[2]) / 3; 				// convert to greyscale
    		int correctValue = value*newSteps;							//kontraskorrigierter Wert
    		++histogram[correctValue];									//in histogram geschmissen
    		rgb[0]=rgb[1]=rgb[2]= correctValue;
    		Utils.RGB2pixel(rgb);										//neuer grauwert zurück ins bild
		}
	}	
}
