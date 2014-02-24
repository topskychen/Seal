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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import spatialindex.Point;

/**
 * @author chenqian
 * Parse data from raw data, and save to file.txt.
 * After the parser, the data format is lat lng timeStamp\t.
 */
public class TDriveParser {

	String 					traFileDirName	= null;
	String 					destFileDirName	= null;
	static int				limit 			= 1000;
	/**
	 * 
	 */
	public TDriveParser(String traFileDirName, String destFileDirName) {
		this.traFileDirName = traFileDirName;
		this.destFileDirName = destFileDirName;
	}
	
	public double parseTime(String source) {
		DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		try {
			Date dt = formatter.parse(source);
			return dt.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	private Trajectory loadData(File dataFile) {
		try {
			Scanner in = new Scanner(new BufferedInputStream(new FileInputStream(dataFile)));
			Trajectory tra = new Trajectory();
			while(in.hasNext()) {
				String[] tks = in.nextLine().split(",");
				Point point = new Point(new double[]{Double.parseDouble(tks[2]), Double.parseDouble(tks[3])});
				Data data = new Data(point, parseTime(tks[1]));
				tra.add(data);
			}
			in.close();
			return tra;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void run() {
		File traFileDir = new File(traFileDirName);
		PrintWriter pw = null;
		int id = 0;
		try {
			pw = new PrintWriter(new BufferedOutputStream(new FileOutputStream(new File(destFileDirName))));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (File subDir : traFileDir.listFiles()) {
			if (!subDir.isDirectory()) continue;
			System.out.print(subDir);
			for (File dataFile : subDir.listFiles()) {
				Trajectory tra = loadData(dataFile);
				if (tra.tra.size() == 0) continue;
				id ++;
				pw.println(tra);
				tra = null;
				if (limit != -1 && id >= limit) break;
			}
			System.out.println(".");
			if (limit != -1 && id >= limit) break;
		}
		pw.close();
		System.out.println("loadFin!");
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String traFileDir = null;
		String destFile = null;
		if (args.length == 0) {
			traFileDir = "./data/trajectory";
			destFile = "./data/TDrive" + (limit == -1 ? "" : limit) + ".txt";
		} else {
			traFileDir = args[0];
			destFile = args[1];
		}
		TDriveParser parser = new TDriveParser(traFileDir, destFile);
		parser.run();
	}

	
}
