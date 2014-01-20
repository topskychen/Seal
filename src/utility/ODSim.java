/**
 * 
 */
package utility;

import index.SearchIndex.INDEX_TYPE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import crypto.AES;
import party.Client;
import party.DataOwner;
import party.ServiceProvider;
import party.TrustedRegister;
import utility.EncFun.ENC_TYPE;

/**
 * @author chenqian
 *
 */
public class ODSim extends Simulator {

	String fileName = "./data/OD100000";
	/**
	 * @param trustedRegister
	 * @param dataOwners
	 * @param serviceProvider
	 * @param client
	 */
	public ODSim() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public ODSim(String fileName) {
		this.fileName = fileName;
	}

	/* (non-Javadoc)
	 * @see utility.Simulator#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		dataOwners 			= new ArrayList<DataOwner>();
		serviceProvider 	= new ServiceProvider();
		client 				= new Client();
		TrustedRegister.sk 	= AES.getSampleKey();
		TrustedRegister.specifyEncFun(ENC_TYPE.Paillier, fileName);
		serviceProvider.specifyIndex(INDEX_TYPE.BTree);
	}
	
	

	/* (non-Javadoc)
	 * @see utility.Simulator#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		// Data owners prepare data 
		DataOwner.initOneDim(dataOwners, fileName);
		
		// Service Provider collects data.
		// Currently, the index will not be stored to file.
		serviceProvider.collectDataOnce(dataOwners);		
		// Client Make queries
//		client
		client.rangeQuery(serviceProvider, fileName);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ODSim odSim = null;
		if (args.length != 0) {
			odSim = new ODSim(args[0]);
		} else {
			odSim = new ODSim();
		}
		odSim.init();
		odSim.run();			
	}

}
