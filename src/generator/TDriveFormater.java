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
import utility.Constants;

/**
 * @author chenqian
 * Format the data to the range and to the format read by the sim.
 */
public class TDriveFormater {

	String 					rawFileName = null;
	String 					plFileName 	= null;
	ArrayList<Trajectory> 	tras 		= null;
	public static Region 	bounds 		= new Region(new double[]{115.959899, 39.686053}, 
											new double[]{116.833313, 40.182821});
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
		coords[0] = (point.getCoord(0) - bounds.getLow(0)) 
				/ (bounds.getHigh(0) - bounds.getLow(0)) * Constants.BOUND;
		coords[1] = (point.getCoord(1) - bounds.getLow(1)) 
				/ (bounds.getHigh(1) - bounds.getLow(1)) * Constants.BOUND;
		return new Point(coords);
	} 
	
	private void formatData() {
		updateBounds();
		for (Trajectory tra : tras) {
			int j = 0;
			for (int i = 0; i < tra.tra.size(); i ++) {
				Data data = tra.tra.get(i);
				if (!bounds.contains(data.point)) continue;
				data.point = formatPoint(data.point);
				data.tiStp = data.tiStp - lowTime;
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
//				if (bounds == null) {
//					bounds = new Region(data.point, data.point);
//				} else {
//					bounds = bounds.combinedRegion(new Region(data.point, data.point));
//				}
				if (data.tiStp > lowTime) lowTime = data.tiStp;
			}
		}
		System.out.println("bounds updated:" + bounds);
	}

	/**
	 * Load data from raw file and get the bounds.
	 * @param rawFileName2
	 */
	private void loadData() {
		try {
			Scanner in = new Scanner(
					new BufferedInputStream(
							new FileInputStream(new File(rawFileName))));
			while(in.hasNext()) {
				in.nextLine(); // id
				tras.add(Trajectory.parseTra(in.nextLine()));
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
			rawFileName = "./data/TDrive1000.txt";
			plFileName = "./data/TDrive1000.pl";
		} else {
			rawFileName = args[0];
			plFileName = args[1];
		}
		TDriveFormater formater = new TDriveFormater(rawFileName, plFileName);
		formater.run();
	}

}
