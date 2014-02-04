/**
 * 
 */
package generator;

import index.Point;
import io.P;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import utility.Constants;

/**
 * @author chenqian
 *
 */
public class TDGenerator {

	Random random 		= new Random();
	Point[][] data		= null;
	
	public Point[] generate(int no) {
		HashSet<Point> points = new HashSet<>();
		for (int i = 0; i < no; i ++) {
			Point p = new Point(random.nextInt(Constants.BOUND), random.nextInt(Constants.BOUND));
			while(points.contains(p)) {
				p = new Point(random.nextInt(Constants.BOUND), random.nextInt(Constants.BOUND));
			}
			points.add(p);
		}
		return points.toArray(new Point[0]);
	}
	
	/**
	 * The no. of owners, the length of trajectory, and the output file name.
	 * @param ownerNo
	 * @param traLen
	 * @param fileName
	 */
	public TDGenerator(int ownerNo, int traLen, String fileName) {
		File file = new File(fileName + ".pl");
		if (file.exists()) {
			System.out.println("file " + fileName + ".pl exists, please delete it first.");
			return;
		}
		
		data = new Point[traLen][];
		for (int j = 0; j < traLen; j ++) {
			data[j] = generate(ownerNo);
		}
		saveToFile(ownerNo, traLen, file);
		System.out.println("Gen fin!");
	}
	
	public void saveToFile(int ownerNo, int traLen, File file) {
		
		try {
			PrintWriter pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)));
			for (int i = 0; i < ownerNo; i ++) {
				pw.println(i);
				for (int j = 0; j < traLen; j ++) {
					if (j != 0) pw.print("\t");
					for (int k = 0; k < data[j][i].getDim(); k ++) {
						if (k != 0) pw.print(" ");
						pw.print(data[j][i].getCoord(k));
					}
				}
				pw.println();
			}
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			file.delete();
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 3) {
			new TDGenerator(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
		} else if (args.length == 0) {
			new TDGenerator(1000, 10, "./data/TD1000");
		} else {
			System.out.println("Error!");
		}
	}
}
