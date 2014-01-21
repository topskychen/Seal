/**
 * 
 */
package generator;

import index.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * @author chenqian
 *
 */
public class TDGenerator {

	Random random = new Random();
	
	/**
	 * 
	 */
	public TDGenerator(int size, String fileName) {
		File file = new File(fileName + ".pl");
		if (file.exists()) {
			System.out.println("file " + fileName + ".pl exists, please delete it first.");
			return;
		}
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			Point[] data = new Point[size];
			for (int i = 0; i < size; i ++) {
				data[i] = new Point(Math.abs(random.nextInt(1 << 20)), 
						Math.abs(random.nextInt(1 << 20)));
			}
			for (int i = 0; i < size; i ++) {
				pw.println(data[i].getCoord(0) + " " + data[i].getCoord(1));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Gen fin!");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			new TDGenerator(Integer.parseInt(args[0]), args[1]);
		} else if (args.length == 0) {
			new TDGenerator(10, "./data/TD10");
		} else {
			System.out.println("Error!");
		}
	}
}
