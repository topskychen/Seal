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

	String fileName = "./data/OD1000";
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

	/* (non-Javadoc)
	 * @see utility.Simulator#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		dataOwners 			= new ArrayList<>();
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
		if (args.length != 0) {
			System.out.println(args[0]);
		} else {}
		ODSim odSim = new ODSim();
		odSim.init();
		odSim.run();
	}

}
