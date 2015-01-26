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

	public void go(String[] args) {
		if (args.length > 0) {
			String mode = args[3];
			this.fileName = args[0];
			if (args[1].equalsIgnoreCase("btree")) {
				this.indexType = INDEX_TYPE.BTree;
			} else if (args[1].equalsIgnoreCase("rtree")) {
				this.indexType = INDEX_TYPE.RTree;
			} else if (args[1].equalsIgnoreCase("qtree")) {
				this.indexType = INDEX_TYPE.QTree;
			} else {
				throw new IllegalStateException("No such tree choice.");
			}
			this.totN = Integer.parseInt(args[2]);
			STAT_INDEX = new StatisticsIndex(this.indexType);
			if (mode.equalsIgnoreCase("rebuild")) {
				this.mode = MODE.REBUILD;
				String queryType = args[4];
				if (queryType.equalsIgnoreCase("rq") || queryType.equalsIgnoreCase("range") || queryType.equalsIgnoreCase("rangequery")) {
					this.queryType = QueryType.range_query;
				} else if (queryType.equalsIgnoreCase("knn")) {
					this.queryType = QueryType.knn;
					this.k = Integer.parseInt(args[5]);
				} else if (queryType.equalsIgnoreCase("skyline")) {
					this.queryType = QueryType.skyline;
				} else {
					System.out.println("no query type : " + queryType + " is supported");
				}
			} else if (mode.equalsIgnoreCase("update")) {
				this.mode = MODE.UPDATE;
			} else if (mode.equalsIgnoreCase("lazy")) {
				this.mode = MODE.LAZY;
				this.updateRate = Double.parseDouble(args[4]);
				this.bufferSize = Integer.parseInt(args[5]);
			} else if (mode.equalsIgnoreCase("loose")) {
				this.mode = MODE.LOOSE;
				this.updateRate = Double.parseDouble(args[4]);
				this.rteeRegionL = Double.parseDouble(args[5]);
			} else {
				System.out.println("This mode is not supprted!");
				return;
			}
			System.out.println("parse fin!");
		}
//			System.out
//					.println("The args should be [fileName indexType totN mode [update_ratio]].");
		
		batchQuery();
		
		System.out.println(this.STAT_INDEX.toString());
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
			} else {
				System.out.println("-------------------- skyline ---------------------");
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
	 * the args should be [fileName indexType totN mode [queryType,update_ratio]].
	 * @param args
	 */
	public static void main(String[] args) {
		Sim sim = new Sim();
		if (args.length == 0) {
			sim.go(new String[] {"./data/GO", "rtree", "1000", "lazy", "0.1", "150"});
		} else {
			sim.go(args);
		}
	}
}
