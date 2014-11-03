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
		DataOwner.update(dataOwners, runId, this);
		
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
		for (double size : Global.QUERY_SIZES) {
			System.out.println("--------------------" + "Query Size " + size + "---------------------");
			querySize = size;
			singleQuery();
		}
	}

	/**
	 * Multi queries for same query ratio
	 * @param sim
	 * @param ratio
	 */
	public void singleQuery() {
		init();
		run(0);
		if (mode != MODE.REBUILD) {
			for (int i = 1; i < Global.RUN_TIMES; i++) {
				System.out.println("--------------------" + i
						+ "---------------------");
				run(i);
			}
		}
		printStat();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sim sim;
		if (args.length > 0) {
			String mode = args[2];
			if (mode.equalsIgnoreCase("rebuild")) {
				sim = new Sim(args[0], args[1], args[2], MODE.REBUILD);
			} else if (mode.equalsIgnoreCase("update")) {
				sim = new Sim(args[0], args[1], args[2], MODE.UPDATE);
				sim.updateRate = Double.parseDouble(args[4]);
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
		
		if (Global.DO_COST) {
			System.out.println(Global.STAT_DO.toString());
		}
		if (Global.INDEX_COST) {
			System.out.println(Global.STAT_INDEX.toString());
		}
	}
}
