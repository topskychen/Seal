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
public class UpdateIDGenerator {

	String				fileDir		= null;
	Random				random		= new Random();
	HashSet<Integer>	dict		= null;
	int 				totN		= 0;
	
	/**
	 * 
	 */
	public UpdateIDGenerator(String fileDir, int totN) {
		this.fileDir = fileDir;
		this.totN = totN;
	}

	public void run() {
		for (double ratio : Global.UPDATE_RATES) {
			PrintWriter pw = null;
			String filePath = fileDir + "_" + ratio + "_" + totN + ".up";
			try {
				pw = new PrintWriter(filePath);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int runTime = 1; runTime < Global.RUN_TIMES; runTime++) {
				genIds((int) (totN * ratio));
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
		int id = random.nextInt(totN);
		while (dict.contains(id)) {
			id = random.nextInt(totN);
		}
		return id;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			UpdateIDGenerator gen = new UpdateIDGenerator(Global.TEST_FILE_DIR + "/GO", 1000);
			gen.run();
		} else {
			UpdateIDGenerator gen = new UpdateIDGenerator(Global.TEST_FILE_DIR + "/GO", Integer.parseInt(args[0]));
			gen.run();
		}
	}

}
