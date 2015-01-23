package generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

import spatialindex.IShape;
import utility.Global;

public class QueryGenerator {
	
	Random random = new Random();
	List<IShape> locations = null;
	int dim;
	
	double[] QUERY_SIZES = {0.032, 0.064, 0.128, 0.256, 0.512, 0.99};
	
	int[] genRange(int b0, int b1, double ratio) {
		int bound = b1 - b0;
		int l = random.nextInt((int) (bound * (1 - ratio))) + b0;
		return new int[] {l, l + (int) (bound * ratio)};
	}

	void genQueries() {
		/**
		 * query
		 */
		try {
			for (double querySize : QUERY_SIZES) {
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
	
	
	public QueryGenerator(int dim) {
		this.dim = dim;
	}
	
	public static void main(String[] args) {
		QueryGenerator gen = new QueryGenerator(3);
		gen.genQueries();
	}

}
