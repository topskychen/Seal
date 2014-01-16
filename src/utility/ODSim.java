/**
 * 
 */
package utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import party.Client;
import party.DataOwner;
import party.ServiceProvider;
import party.TrustedRegister;

/**
 * @author chenqian
 *
 */
public class ODSim extends Simulator {

	/**
	 * @param trustedRegister
	 * @param dataOwners
	 * @param serviceProvider
	 * @param client
	 */
	public ODSim(TrustedRegister trustedRegister,
			ArrayList<DataOwner> dataOwners, ServiceProvider serviceProvider,
			Client client) {
		super(trustedRegister, dataOwners, serviceProvider, client);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see utility.Simulator#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		// Data owners prepare data 
		DataOwner.initOneDim(dataOwners, "");
		
		// Service Provider collects data.
		// Currently, the index will not be stored to file.
		serviceProvider.collectDataOnce(dataOwners);
		
		// Client Make queries
//		client
		client.rangeQuery(serviceProvider, "");
	}
	
	

	/* (non-Javadoc)
	 * @see utility.Simulator#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
