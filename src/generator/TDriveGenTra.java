package generator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import party.DataOwner;
import spatialindex.IShape;
import spatialindex.Point;
import utility.Global;

public class TDriveGenTra {

	int traLen;
	String fileName;
	ArrayList<HashMap<Integer, IShape>>	pointss	= new ArrayList<HashMap<Integer, IShape>>();
	ArrayList<Trajectory>	tras		= null;
	
	public TDriveGenTra() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @param len, the trajectory length i want 
	 * @param fileName, the original trajectory
	 */
	public TDriveGenTra(String fileName) {
		this.fileName = fileName;
		load();		
	}
	
	public void gen(int len) {
		traLen = len;
		genData();
		saveToFile();
	}
	
	
	private void genData() {
		int totn = pointss.size();
		int startTime = 0;
		int runTimes = 55;
		tras = new ArrayList<Trajectory>();
		int loop = totn;
		for (int i = 0; ; i++) {
			if ((i + 1) % totn == 0) {
				if (loop == totn) {
					System.out.println("loop is found!");
					break;
				}
				startTime += 5;
				loop = totn;
			}
			int id = i % totn;
			HashMap<Integer, IShape> points = pointss.get(id);
			Trajectory tra = new Trajectory();
			for (int t = startTime; t < startTime + runTimes; t++) {
				if (points.get(t) == null) break;
				tra.add(new Data((Point) points.get(t), t - startTime));
			}
			if (tra.tra.size() == 0) continue;
			loop--;
			tras.add(tra);
			if (tras.size() >= traLen) break;
		}
	}

	public void saveToFile() {
		int id = 0;
		try {
			PrintWriter pw = new PrintWriter(
					new BufferedOutputStream(
							new FileOutputStream(new File(fileName + "_" + traLen + ".pl"))));
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
	
	/**
	 * Load init trajectory data
	 */
	public void load() {
		File file = new File(fileName + ".pl");
		if (file.exists()) {
			try {
				Scanner in = new Scanner(new BufferedInputStream(
						new FileInputStream(file)));
				while (in.hasNext()) {
					int id = Integer.parseInt(in.nextLine());
					HashMap<Integer, IShape> points = DataOwner.parsePoints(id,
							in.nextLine(), 0, 550000, null); // a large length 
					pointss.add(points);
				}
				in.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("File " + file + " is not existed!");
		}
	}

	public static void main(String[] args) {
		TDriveGenTra gen = new TDriveGenTra(Global.TEST_FILE_DIR + "/TDrive");
		Scanner in = new Scanner(System.in);
		while (true) {
			System.out.println("Please input the tra len you want:");
			int len = Integer.parseInt(in.nextLine());
			gen.gen(len);
		}
	}
}
