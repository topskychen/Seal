package generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

import spatialindex.Point;
import spatialindex.Region;
import utility.Global;

/**
 * Created by chenqian on 25/2/14.
 */
public class TDriveQGen {
	// public static double[] RATIOS = {0.0025, 0.005, 0.01, 0.02, 0.04, 0.08,
	// 0.16};

	public String	fileDir		= null;
	public int		runTimes	= 0;
	Random			random		= new Random();

	public TDriveQGen(String fileDir, int runTimes) {
		this.fileDir = fileDir;
		this.runTimes = runTimes;
	}

	public void run() {
		for (double ratio : Global.RATIOS) {
			PrintWriter pw = null;
			String filePath = fileDir + "_" + ratio + ".qr";
			try {
				pw = new PrintWriter(new File(filePath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			for (int runTime = 0; runTime < runTimes; runTime++) {
				Region region = genQuery(ratio);
				pw.print((int) region.getLow(0) + " " + (int) region.getLow(1)
						+ " ");
				pw.println((int) region.getHigh(0) + " "
						+ (int) region.getHigh(1));
			}
			pw.close();
			System.out.println(filePath);
		}
	}

	private double nextDouble(double x) {
		double ans = -1;
		while (ans < 0 || ans > 1) {
			ans = random.nextGaussian() * x + x;
		}
		return ans;
	}

	private Region genQuery(double ratio) {
		double width = Global.BOUND
				* ((1 - ratio)
						* nextDouble(((Math.sqrt(ratio) - ratio) / (1 - ratio))) + ratio);
		double height = Global.BOUND * Global.BOUND * ratio / width; // it
																		// may
																		// be
																		// different
																		// from
																		// width
		// Point low = new Point(new double[]{100, 100});
		Point low = new Point(new double[] {
				random.nextInt((int) (Global.BOUND - width)),
				random.nextInt((int) (Global.BOUND - height)) });
		Point high = new Point(new double[] { low.getCoord(0) + width,
				low.getCoord(1) + height });
		return new Region(low, high);
	}

	public static void main(String[] args) {
		String fileDir = null;
		int runTimes = 1000;
		if (args.length == 0) {
			fileDir = Global.TEST_FILE_DIR + "/TDrive";
		} else {
			fileDir = args[0];
			runTimes = Integer.parseInt(args[1]);
		}
		TDriveQGen gen = new TDriveQGen(fileDir, runTimes);
		gen.run();
	}
}
