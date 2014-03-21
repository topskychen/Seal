/**
 * 
 */
package generator;

import graphics.Data;
import graphics.DrawCollection;
import graphics.ShowData;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import spatialindex.Region;
import utility.Global;

/**
 * @author chenqian
 * 
 */
public class TDriveShower {

	String					fileName	= null;
	String					qrFileName	= null;
	ArrayList<Trajectory>	tras		= null;
	ArrayList<Region>		queries		= null;
	int						queryLen	= 5;
	int						startTime	= 0;
	int						timeLen		= 10;
	int						timeStpS	= 0;
	int						timeStpE	= 1000;

	/**
     *
     */
	public TDriveShower(String fileName, String qrFileName) {
		this.fileName = fileName;
		this.qrFileName = qrFileName;
	}

	public void run() {
		loadData();
		ArrayList<Data> datas = new ArrayList<Data>();
		for (int i = 0; i < tras.size(); i++) {
			prepareData(tras.get(i), datas);
		}
		for (int i = 0; i < queries.size(); i++) {
			datas.add(new Data(queries.get(i), Color.RED, Data.DrawType.Region));
		}
		ShowData showData = new ShowData(new DrawCollection(datas));
		ShowData.draw(showData);
	}

	private void prepareData(Trajectory trajectory, ArrayList<Data> datas) {
		ArrayList<generator.Data> tmpDatas = new ArrayList<generator.Data>();
		for (int i = startTime; i < startTime + timeLen; i++) {
			if (i < trajectory.tra.size()) {
				generator.Data data = trajectory.tra.get(i);
				if (data.tiStp >= timeStpS && data.tiStp <= timeStpE)
					tmpDatas.add(data);
			}
		}
		for (int i = 0; i < tmpDatas.size(); i++) {
			datas.add(new Data(tmpDatas.get(i).point, Color.BLACK,
					(i == 0) ? Data.DrawType.Point : Data.DrawType.Line));
		}
	}

	/**
	 * Load data from raw file and get the bounds.
	 */
	private void loadData() {
		tras = new ArrayList<Trajectory>();
		queries = new ArrayList<Region>();
		int lineNo = 0;
		try {
			Scanner in = new Scanner(new BufferedInputStream(
					new FileInputStream(new File(fileName))));
			while (in.hasNext()) {
				in.nextLine(); // this is the id
				tras.add(Trajectory.parseTraInt(in.nextLine()));
			}
			in.close();
			in = new Scanner(new BufferedInputStream(new FileInputStream(
					qrFileName)));
			while (in.hasNext()) {
				++lineNo;
				if (lineNo > queryLen)
					break;
				String line = in.nextLine();
				String[] tks = line.split(" ");
				Region region = new Region(
						new double[] { Double.parseDouble(tks[0]),
								Double.parseDouble(tks[1]) }, new double[] {
								Double.parseDouble(tks[2]),
								Double.parseDouble(tks[3]) });
				queries.add(region);
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
		TDriveShower shower = new TDriveShower(Global.TEST_FILE_DIR
				+ "/TDrive.pl", Global.TEST_FILE_DIR + "/TDrive_0.16.qr");
		shower.run();
	}

}
