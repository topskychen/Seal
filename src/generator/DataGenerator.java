/**
 * 
 */
package generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import spatialindex.IShape;
import spatialindex.Point;
import utility.Global;

/**
 * @author chenqian
 *
 */
public class DataGenerator {
	Random random = new Random();
	List<IShape> locations = null;
	int dim, totN;
	
	
	int[] genRange(int b0, int b1, double ratio) {
		int bound = b1 - b0;
		int l = random.nextInt((int) (bound * (1 - ratio))) + b0;
		return new int[] {l, l + (int) (bound * ratio)};
	}
	
	String printPoint(Point p) {
		String res = "";
		for (int i = 0; i < p.getDimension(); ++i) {
			res += p.getCoord(i) + " ";
		}
		res.trim();
		return res;
	}
	
	double margin(double v) {
		if (v < 0) return 0;
		if (v > Global.BOUND) return Global.BOUND;
		return v;
	}
	
	public void genData() {
		String fileName = Global.TEST_FILE_DIR + "/GO";
		/**
		 * data
		 */
		try {
			PrintWriter pw = new PrintWriter(new File(fileName + "_" + dim + "_" + totN + ".pl"));
			int id = 0;
			for (IShape location : locations) {
				double[] coords = new double[dim];
				coords[0] = ((Point) location).getCoord(0);
				coords[1] = ((Point) location).getCoord(1);
				for (int i = 2; i < dim; ++i) {
					coords[i] = margin((random.nextGaussian() * 30000) + 50000);
				}
				String line = "";
				Point p = new Point(coords);
				line += printPoint(p);
				for (int j = 1; j < Global.RUN_TIMES; ++j) {
					for (int i = 2; i < dim; ++i) {
						coords[i] = margin(coords[i] + (random.nextDouble() - 0.5) * 5000);
					}
					p = new Point(coords);
					line += "\t" + printPoint(p);
				}
				pw.println(line);
				if (++id>= totN) break;
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("data fin!");
	}

	
	
	/**
	 * 
	 */
	public DataGenerator(int dim, int totN) {
		
		this.dim = dim;
		this.totN = totN;
		
		locations = LocationParser.parseLocation("./data/Location.txt");
		
		genData();
	}		
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			DataGenerator gen = new DataGenerator(3, 1000);
		} else {
			DataGenerator gen = new DataGenerator(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		}
	}

}
