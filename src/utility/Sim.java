/**
 * 
 */
package utility;

import index.MemQTree;
import index.MemRTree;
import index.SearchIndex.INDEX_TYPE;

import java.util.ArrayList;

import party.Client;
import party.DataOwner;
import party.ServiceProvider;
import party.TrustedRegister;
import utility.EncFun.ENC_TYPE;
import utility.Global.MODE;
import crypto.AES;

/**
 * @author chenqian
 * 
 */
public class Sim extends Simulator {

	String		fileName	= Global.TEST_FILE_DIR + "/TDrive";
	INDEX_TYPE	type		= INDEX_TYPE.QTree;

	public Sim() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Sim(String fileName, String type) {
		this.fileName = fileName;
		if (type.equalsIgnoreCase("btree")) {
			this.type = INDEX_TYPE.BTree;
		} else if (type.equalsIgnoreCase("rtree")) {
			this.type = INDEX_TYPE.RTree;
		} else if (type.equalsIgnoreCase("qtree")) {
			this.type = INDEX_TYPE.QTree;
		} else {
			throw new IllegalStateException("No such tree choice.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utility.Simulator#init()
	 */
	@Override
	public void init(int startTime, int runTimes) {
		// TODO Auto-generated method stub;
		clearStat();
		Global.G_QTREE = new MemQTree(4, Global.G_BOUND, 0, 0);
		Global.G_RTREE = MemRTree.createTree();
		Global.G_RTREE.setRecordStatus(true);
		dataOwners = new ArrayList<DataOwner>();
		serviceProvider = new ServiceProvider(statU);
		client = new Client(statQ);
		TrustedRegister.sk = AES.getSampleKey();
		TrustedRegister.specifyEncFun(ENC_TYPE.OTPad, fileName);
		DataOwner.initData(dataOwners, fileName, type, startTime, runTimes);
		System.out.println("init done.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utility.Simulator#run()
	 */
	@Override
	public void run(int runId, double ratio) {
		// Data owners prepare data
		DataOwner.prepare(dataOwners, type, runId);

		// Service Provider collects data.
		// Currently, the index will not be stored to file.
		serviceProvider.collectDataOnce(dataOwners, type, runId);
		// Client Make queries
		// client
		if (ratio != -1) {
			client.rangeQuery(serviceProvider, fileName + "_" + ratio, runId);
		} else {
			client.rangeQuery(serviceProvider, fileName, runId);
		}
	}

	public static void batchQuery(Sim sim, int startTime, int runTimes) {
		for (double ratio : Global.RATIOS) {
			System.out.println("--------------------" + ratio
					+ "---------------------");
			singleQuery(sim, startTime, runTimes, ratio);
		}
	}

	public static void singleQuery(Sim sim, int startTime, int runTimes,
			double ratio) {
		sim.init(startTime, runTimes);
		for (int i = startTime; i < startTime + runTimes; i++) {
			if (!Global.BATCH_QUERY)
				System.out.println("--------------------" + i
						+ "---------------------");
			sim.run(i, ratio);
		}
		sim.printStat();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sim sim;
		int startTime = 0, runTimes = 100;
		if (args.length == 6 || args.length == 7) {
			sim = new Sim(args[0], args[1]);
			startTime = Integer.parseInt(args[2]);
			runTimes = Integer.parseInt(args[3]);
			Global.QUERY_LIM = Integer.parseInt(args[4]);
			String mode = args[5];
			if (mode.equalsIgnoreCase("rebuild"))
				Global.G_MODE = MODE.REBUILD;
			else if (mode.equalsIgnoreCase("update")) {
				Global.G_MODE = MODE.UPDATE;
				Global.UPDATE_RT = Double.parseDouble(args[6]);
			} else if (mode.equalsIgnoreCase("lazy"))
				Global.G_MODE = MODE.LAZY;
			else {
				System.out.println("This mode is not supprted!");
				return;
			}
			System.out.println("Init fin!");
		} else if (args.length == 0) {
			sim = new Sim();
		} else {
			System.out
					.println("The args should be [fileName treeType startTime runTimes queryLen mode [update_ratio]].");
			return;
		}

		if (Global.BATCH_QUERY) {
			batchQuery(sim, startTime, runTimes);
		} else {
			singleQuery(sim, startTime, runTimes, -1);
		}
		if (Global.DO_COST) {
			System.out.println(Global.STAT_DO.toString());
		}
		if (Global.INDEX_COST) {
			System.out.println(Global.STAT_INDEX.toString());
		}
	}
}
