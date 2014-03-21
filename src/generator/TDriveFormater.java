/**
 * 
 */
package generator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import spatialindex.Point;
import spatialindex.Region;
import utility.Global;

/**
 * @author chenqian
 * Format the data to the range and to the format read by the sim.
 */
public class TDriveFormater {

	String 					rawFileName = null;
	String 					plFileName 	= null;
	ArrayList<Trajectory> 	tras 		= null;
	double					lowTime		= Double.MAX_VALUE;
	
	/**
	 * 
	 */
	public TDriveFormater(String rawFileName, String plFileName) {
		this.rawFileName = rawFileName;
		this.plFileName = plFileName;
		tras = new ArrayList<Trajectory>();
	}
	
	public void run() {
		loadData();
		formatData();
		saveData();
	}

	private void saveData() {
		int id = 0;
		try {
			PrintWriter pw = new PrintWriter(
					new BufferedOutputStream(
							new FileOutputStream(new File(plFileName))));
			for (Trajectory tra : tras) {
				if (tra.tra.size() == 0) continue;
				pw.println(id ++);
				pw.println(tra.toStringInt());
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("data saved. [" + id + "]");
	}

	private Point formatPoint(Point point) {
		double[] coords = new double[2];
		coords[0] = (point.getCoord(0) - Global.BOUNDS.getLow(0))
				/ (Global.BOUNDS.getHigh(0) - Global.BOUNDS.getLow(0)) * Global.BOUND;
		coords[1] = (point.getCoord(1) - Global.BOUNDS.getLow(1))
				/ (Global.BOUNDS.getHigh(1) - Global.BOUNDS.getLow(1)) * Global.BOUND;
		return new Point(coords);
	} 
	
	private void formatData() {
		updateBounds();
		for (Trajectory tra : tras) {
			int j = 0;
			for (int i = 0; i < tra.tra.size(); i ++) {
                if (j > Global.TRA_LEN) {
                    break;
                }
				Data data = tra.tra.get(i);
				if (!Global.BOUNDS.contains(data.point)) {
                    continue;
                }
				data.point = formatPoint(data.point);
				data.tiStp = (data.tiStp - lowTime) / 1000;
				tra.tra.add(j ++, data);
			}
			while (j < tra.tra.size()) {
				tra.tra.remove(j);
			}
		}
        System.out.println("data formated");
	}

	private void updateBounds() {
		for (Trajectory tra : tras) {
			for (Data data : tra.tra) {
				if (data.tiStp < lowTime) lowTime = data.tiStp;
			}
		}
		System.out.println("bounds updated:" + Global.BOUNDS + ", " + lowTime);
	}

	/**
	 * Load data from raw file and get the bounds.
	 */
	private void loadData() {
		try {
			Scanner in = new Scanner(
					new BufferedInputStream(
							new FileInputStream(new File(rawFileName))));
			while(in.hasNext()) {
				String line = in.nextLine();
				tras.add(Trajectory.parseTra(line));
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("load done");
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String rawFileName = null;
		String plFileName = null;
		if (args.length == 0) {
			rawFileName = Global.TEST_FILE_DIR + "/TDrive.txt";
			plFileName = Global.TEST_FILE_DIR + "/TDrive.pl";
		} else {
			rawFileName = args[0];
			plFileName = args[1];
		}
		TDriveFormater formater = new TDriveFormater(rawFileName, plFileName);
		formater.run();
	}

}
