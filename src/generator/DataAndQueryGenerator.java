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
public class DataAndQueryGenerator {
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
				if (id++ >= totN) break;
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("data fin!");
	}

	void genQueries() {
		/**
		 * query
		 */
		try {
			for (double querySize : Global.QUERY_SIZES) {
				String fileName = Global.TEST_FILE_DIR + "/GO";
				PrintWriter pw = new PrintWriter(new File(fileName + "_" + dim + "_" + querySize + ".rq"));
				double ratio = Math.pow(querySize, 1.0 / dim);
				for (int i = 0; i < 100; ++i) {
					int[] low = new int[dim];
					int[] high = new int[dim];
					for (int j = 0; j < dim; ++j) {
						int[] range = genRange(0, (int) Global.BOUND, ratio);
						low[j] = range[0];
						high[j] = range[1];
					}
					String line = "";
					for (int j = 0; j < dim; ++j) {
						line += low[j] + " ";
					}
					for (int j = 0; j < dim; ++j) {
						line += high[j] + " ";
					}
					line.trim();
					pw.println(line);
				}
				pw.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("query fin!");	
	}
	
	
	/**
	 * 
	 */
	public DataAndQueryGenerator(int dim, int totN) {
		
		this.dim = dim;
		this.totN = totN;
		
		locations = LocationParser.parseLocation("./data/Location.txt");
		
		genData();
		genQueries();
	}		
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataAndQueryGenerator gen = new DataAndQueryGenerator(3, 1000);
	}

}
