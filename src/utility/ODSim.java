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
		this.dataOwners = new ArrayList<>();
		ArrayList<Integer> values = loadValuesFromFile("");
		for (int i = 0; i < values.size(); i ++) {
			dataOwners.get(i).addValue(values.get(i));
			dataOwners.get(i).prepareSeals();
		}
		
		// Service Provider collects data.
		this.serviceProvider = new ServiceProvider();
		serviceProvider.collectDataOnce(dataOwners);
		
		// Client Make queries
		this.client = new Client();
	}
	
	public ArrayList<Integer> loadValuesFromFile(String file) {
		try {
			ArrayList<Integer> data = new ArrayList<>();
			Scanner in = new Scanner(new File(file));
			while(in.hasNext()) {
				data.add(Integer.parseInt(in.nextLine()));
			}
			in.close();
			return data;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
