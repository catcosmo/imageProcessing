import java.util.Random;


public class MergeSort {
	public static void mergeTest() {
		long timetotal = 0;
		long timeStart = System.currentTimeMillis();
		int[] c = QuickSort.randomIntArr();
		int[] d = QuickSort.randomIntArr();
		for(int i = 0; i < 100; i++){	    
			merge(c, d);                                  
		    long timeEnd = System.currentTimeMillis(); 
		    timetotal += timeEnd-timeStart;
		}
		timetotal = timetotal/100;
		System.out.println("Für zwei Arrays mit gesamtlänge" + (c.length+d.length) + " Zahlen, ist die durchschnittliche Laufzeit für Mergesort " + timetotal + " Millisekunden.");
	  }
	
	public static int[] merge (int[]a, int[]b) {   
		

		int i=0, j=0, k=0;                           
		int[] c = new int[a.length + b.length];      

		while ((i<a.length) && (j<b.length)) {       // mischen, bis ein Array leer
			if (a[i] < b[j])                           // jeweils das kleinere Element
				c[k++] = a[i++];                       // wird nach c uebernommen
			else
				c[k++] = b[j++];
		}

		while (i<a.length) c[k++] = a[i++];          // ggf.: Rest von Folge a
		while (j<b.length) c[k++] = b[j++];          // ggf.: Rest von Folge b

		return c;                                   
	}
}
