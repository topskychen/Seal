/**
 * 
 */
package generator;

import graphics.Data;
import graphics.ShowData;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author chenqian
 *
 */
public class TDriveShower {

	String fileName = null;
	ArrayList<Trajectory> tras = null;
	/**
	 * 
	 */
	public TDriveShower(String fileName) {
		this.fileName = fileName; 
	}
	
	
	
	public void run() {
		loadData();
		graphics.Data[] datas = new graphics.Data[tras.size()];
		for (int i = 0; i < tras.size(); i ++) {
			datas[i] = prepareData(tras.get(i));
			datas[i].setLineType();
		}
		ShowData showData = new ShowData(datas);
		ShowData.draw(showData);
	}

	private Data prepareData(Trajectory trajectory) {
		ArrayList<spatialindex.Point> points = new ArrayList<spatialindex.Point>();
		for (int i = 0; i < trajectory.tra.size(); i ++) {
			if (TDriveFormater.bounds.contains(trajectory.tra.get(i).point)) {
				points.add(trajectory.tra.get(i).point);
			}
		}
		return new Data(points.toArray(new spatialindex.Point[0]), Color.BLACK);
	}



	/**
	 * Load data from raw file and get the bounds.
	 * @param rawFileName2
	 */
	private void loadData() {
		tras = new ArrayList<Trajectory>();
		try {
			Scanner in = new Scanner(
					new BufferedInputStream(
							new FileInputStream(new File(fileName))));
			while(in.hasNext()) {
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
		TDriveShower shower = new TDriveShower("./data/TDrive.txt");
		shower.run();
	}

}
