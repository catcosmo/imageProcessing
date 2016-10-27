
public class Utils {
	
	public static final int ARGB_WHITE = 0xffffffff;
	public static final int ARGB_BLACK = 0xff000000;

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
	public static int RGB2pixel(int[] RGB) {
		return Utils.writeBackPixels(RGB);
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
	
	public static void initWhite(int[] pixels) {
		for (int i=0; i<pixels.length; ++i) {
			pixels[i] = Utils.ARGB_WHITE;
		}
	}
	
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
	
	public static double distance(int x1, int y1, int x2, int y2) {
		int deltaX = x1-x2;
		int deltaY = y1-y2;
		return Math.sqrt((deltaX*deltaX + deltaY*deltaY));
	}
	
	public static int[] makeBW(int[] pixels, int threshold) {
		
		// convert pixels to BW		
		for(int i = 0; i < pixels.length; i++) {
			int[] RGB = Utils.fetchRGBValues(pixels[i]);
			RGB[0] = RGB[1] = RGB[2] = (RGB[0] + RGB[1] + RGB[2]) / 3; // convert to greyscale
			RGB[0] = RGB[1] = RGB[2] = (RGB[0] <= threshold) ? 0 : 255;
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
	
}
