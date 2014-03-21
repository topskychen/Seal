/**
 * 
 */
package generator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;

import utility.Global;

/**
 * @author chenqian
 * 
 */
public class TDriveUGen {

	String				fileDir		= null;
	int					runTimes	= 0;
	Random				random		= new Random();
	HashSet<Integer>	dict		= null;

	/**
	 * 
	 */
	public TDriveUGen(String fileDir, int runTimes) {
		this.fileDir = fileDir;
		this.runTimes = runTimes;
	}

	public void run() {
		for (double ratio : Global.UPDATE_RTS) {
			PrintWriter pw = null;
			String filePath = fileDir + "_" + ratio + ".ur";
			try {
				pw = new PrintWriter(filePath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int runTime = 0; runTime < runTimes; runTime++) {
				genIds((int) (Global.TOTN * ratio));
				for (int id : dict) {
					pw.print(id + " ");
				}
				pw.println();
			}
			pw.close();
			System.out.println(filePath);
		}
	}

	private void genIds(int num) {
		dict = new HashSet<Integer>();
		for (int i = 0; i < num; i++) {
			int id = nextId();
			dict.add(id);
		}
	}

	private int nextId() {
		int id = random.nextInt(Global.TOTN);
		while (dict.contains(id)) {
			id = random.nextInt(Global.TOTN);
		}
		return id;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TDriveUGen gen = new TDriveUGen(Global.TEST_FILE_DIR + "/TDrive", 1002);
		gen.run();
	}

}
