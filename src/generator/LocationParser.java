package generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import party.DataOwner;
import spatialindex.IShape;
import spatialindex.Point;

public class LocationParser {
	
	List<IShape> points = new LinkedList<IShape>();
	
	
	public static List<IShape> parseLocation(String fileName) {
		File file = new File(fileName);
		List<IShape> res = new LinkedList<IShape>();
		try {
			Scanner in = new Scanner(file);
			while (in.hasNext()) {
				String[] tks = in.nextLine().split(" ");
				res.add(new Point(new double[] {Double.parseDouble(tks[0]), Double.parseDouble(tks[1])}));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	
	public void loadFile(String fileName) {
		File file = new File(fileName);
		try {
			Scanner in = new Scanner(file);
			while (in.hasNext()) {
				in.nextLine(); // readId
				List<IShape> tmp = DataOwner.parsePoints(in.nextLine());
				points.add(tmp.get(0));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void saveFile(String fileName) {
		File file = new File(fileName);
		try {
			PrintWriter pw = new PrintWriter(file);
			for (IShape point : points) {
				pw.println(((Point) point).getCoord(0) + " " + ((Point) point).getCoord(1));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public LocationParser(String inFile, String outFile) {
		loadFile(inFile);
		saveFile(outFile);
		System.out.println("done");
	}

	public static void main(String[] args) {
		new LocationParser("./data/TDrive_1000000.pl", "./data/Location.txt");
	}

}
