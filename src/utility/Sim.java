/**
 * 
 */
package utility;

import index.SearchIndex.INDEX_TYPE;

import java.util.ArrayList;

import crypto.AES;
import party.Client;
import party.DataOwner;
import party.ServiceProvider;
import party.TrustedRegister;
import utility.Constants.MODE;
import utility.EncFun.ENC_TYPE;

/**
 * @author chenqian
 *
 */
public class Sim extends Simulator {

	String 				fileName 	= "./data/TDrive";
	INDEX_TYPE 			type 		= INDEX_TYPE.QTree;


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

	/* (non-Javadoc)
	 * @see utility.Simulator#init()
	 */
	@Override
	public void init(int runTimes) {
        // TODO Auto-generated method stub
        dataOwners 			= new ArrayList<DataOwner>();
        serviceProvider 	= new ServiceProvider(statU);
        client 				= new Client(statQ);
        TrustedRegister.sk 	= AES.getSampleKey();
        TrustedRegister.specifyEncFun(ENC_TYPE.OTPad, fileName);
        DataOwner.initData(dataOwners, fileName, type, runTimes);
    }

	

	/* (non-Javadoc)
	 * @see utility.Simulator#run()
	 */
	@Override
	public void run(int runId) {
		// TODO Auto-generated method stub
		// Data owners prepare data 
		DataOwner.prepare(dataOwners, type, runId);
		
		// Service Provider collects data.
		// Currently, the index will not be stored to file.
		serviceProvider.collectDataOnce(dataOwners, type, runId);		
		// Client Make queries
		//		client
		client.rangeQuery(serviceProvider, fileName, runId);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Sim sim;
		int runTimes 	= 10;
		if (args.length == 4) {
			sim = new Sim(args[0], args[1]);
			runTimes = Integer.parseInt(args[2]);
			String mode = args[3];
			if (mode.equalsIgnoreCase("rebuild")) Constants.G_MODE = MODE.REBUILD;
			else if (mode.equalsIgnoreCase("update")) Constants.G_MODE = MODE.UPDATE;
			else if (mode.equalsIgnoreCase("lazy")) Constants.G_MODE = MODE.LAZY;
			else {
				System.out.println("This mode is not supprted!");
				return;
			}
		} else if (args.length == 0){
			sim = new Sim();
		} else {
			System.out.println("The args should be [fileName treeType ruTimes mode].");
			return;
		}
		sim.init(runTimes);
		System.out.println("Init fin!");
		for (int i = 0; i < runTimes; i ++) {
			System.out.println("--------------------"+ i +"---------------------");
			sim.run(i);			
		}
		sim.printStat();
	}

}
