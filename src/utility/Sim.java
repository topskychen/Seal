/**
 * 
 */
package utility;

import index.Query.QueryType;
import index.SearchIndex.INDEX_TYPE;

import java.util.ArrayList;

import party.Client;
import party.DataOwner;
import party.ServiceProvider;
import utility.Global.MODE;

/**
 * @author chenqian
 * 
 */
public class Sim extends Simulator {

	
	
	public Sim() {
		super();
	}

	public Sim(String fileName, String indexType, String totN, MODE mode) {
		this.fileName = fileName;
		if (indexType.equalsIgnoreCase("btree")) {
			this.indexType = INDEX_TYPE.BTree;
		} else if (indexType.equalsIgnoreCase("rtree")) {
			this.indexType = INDEX_TYPE.RTree;
		} else if (indexType.equalsIgnoreCase("qtree")) {
			this.indexType = INDEX_TYPE.QTree;
		} else {
			throw new IllegalStateException("No such tree choice.");
		}
		this.mode = mode;
		this.totN = Integer.parseInt(totN);
		STAT_INDEX = new StatisticsIndex(this.indexType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utility.Simulator#init()
	 */
	@Override
	public void init() {
		clearStat();
		dataOwners = new ArrayList<DataOwner>();
		serviceProvider = new ServiceProvider(statU, this);
		client = new Client(statQ, this);
		DataOwner.initData(dataOwners, this);
		System.out.println("init simulator done.");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see utility.Simulator#run()
	 */
	@Override
	public void run(int runId) {
		//TODO if (runId > 0) //do update
		if (mode != MODE.REBUILD) DataOwner.update(dataOwners, runId, this);
		
		if (queryType == QueryType.range_query) {
			client.rangeQuery(serviceProvider, runId);
		} else if (queryType == QueryType.knn) {
			client.knn(serviceProvider, runId, k);
		} else if (queryType == QueryType.skyline) {
			client.skyline(serviceProvider, runId);
		} else {
			System.out.println("No such query is supported!");
		}
	}

	/**
	 * Batch query for various query sizes
	 * @param sim
	 */
	public void batchQuery() {
		if (mode == MODE.REBUILD) {
			updateRate = Global.UPDATE_RATES[0];
			init();
			DataOwner.update(dataOwners, 0, this);
			if (queryType == queryType.range_query) {
				for (double size : Global.QUERY_SIZES) {
					System.out.println("--------------------" + "Query Size " + size + "---------------------");
					querySize = size;
					client.loadFile(fileName);
					run(0);
					printStat();
				}
			} else if (queryType == queryType.knn) {
				System.out.println("--------------------" + k + " NN " + "---------------------");
				querySize = Global.QUERY_SIZES[0];
				client.loadFile(fileName);
				run(0);
				printStat();
			}
		}
		if (mode != MODE.REBUILD) {
			for (double rate : Global.UPDATE_RATES) {
				System.out.println("--------------------" + "Update Rate " + rate
						+ "---------------------");
				updateRate = rate;
				init();
				client.loadFile(fileName);
				run(0);
				for (int i = 1; i < 3; i++) {
					clearStat();
					run(i);
					printStat();
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sim sim;
		if (args.length > 0) {
			String mode = args[3];
			if (mode.equalsIgnoreCase("rebuild")) {
				sim = new Sim(args[0], args[1], args[2], MODE.REBUILD);
				String queryType = args[4];
				if (queryType.equalsIgnoreCase("knn")) {
					sim.queryType = QueryType.knn;
					sim.k = Integer.parseInt(args[5]);
				}
			} else if (mode.equalsIgnoreCase("update")) {
				sim = new Sim(args[0], args[1], args[2], MODE.UPDATE);
			} else if (mode.equalsIgnoreCase("lazy")) {
				sim = new Sim(args[0], args[1], args[2], MODE.LAZY);
				sim.updateRate = Double.parseDouble(args[4]);
				sim.bufferSize = Integer.parseInt(args[5]);
			} else if (mode.equalsIgnoreCase("loose")) {
				sim = new Sim(args[0], args[1], args[2], MODE.LOOSE);
				sim.updateRate = Double.parseDouble(args[4]);
				sim.rteeRegionL = Double.parseDouble(args[5]);
			} else {
				System.out.println("This mode is not supprted!");
				return;
			}
			System.out.println("parse fin!");
		} else if (args.length == 0) {
			sim = new Sim();
		} else {
			System.out
					.println("The args should be [fileName indexType totN mode [update_ratio]].");
			return;
		}
		
		sim.batchQuery();
		
		System.out.println(sim.STAT_INDEX.toString());
	}
}
