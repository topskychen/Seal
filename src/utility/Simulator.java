/**
 * 
 */
package utility;

import java.util.ArrayList;

import party.Client;
import party.DataOwner;
import party.ServiceProvider;
import party.TrustedRegister;


/**
 * @author chenqian
 *
 */
public abstract class Simulator {

	TrustedRegister 		trustedRegister 		= null;
	ArrayList<DataOwner> 	dataOwners 				= null;
	ServiceProvider 		serviceProvider 		= null;
	Client 					client 					= null;
	double 					preparationTime 		= -1;
	double 					verificationTime 		= -1;
	long 					voSize					= -1;
	
	
	/**
	 * 
	 * @param trustedRegister
	 * @param dataOwners
	 * @param serviceProvider
	 * @param client
	 */
	public Simulator(TrustedRegister trustedRegister,
			ArrayList<DataOwner> dataOwners, ServiceProvider serviceProvider,
			Client client) {
		super();
		this.trustedRegister = trustedRegister;
		this.dataOwners = dataOwners;
		this.serviceProvider = serviceProvider;
		this.client = client;
	}
	

	/**
	 * For initializing the keys and indexes.
	 */
	public abstract void init();
	
	/**
	 * Run multi times
	 */
	public abstract void run();
	
	
	/**
	 * Get the total time of simulation
	 * @return
	 */
	public double getTotalSimulationTime() {
		return getPreparationTime() + getVerificationTime();
	}
	
	/**
	 * Get time of preparation
	 * @return
	 */
	public double getPreparationTime() {
		if (preparationTime < 0) {
			throw new IllegalStateException("The preparationTime is not set, maybe u need to call run function first");
		}
		return preparationTime;
	}
	
	/**
	 * Get time of verification
	 * @return
	 */
	public double getVerificationTime() {
		if (verificationTime < 0) {
			throw new IllegalStateException("The verificationTime time is not set, maybe u need to call run function first");
		}
		return verificationTime;
	}
	
	/**
	 * Get size of VO
	 * @return
	 */
	public long getVOsize() {
		if (voSize < 0) {
			throw new IllegalStateException("The voSize is not set, maybe u need to call run function first");
		}
		return voSize;
	}
	
	/**
	 * Get the infomation.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append("Prepare time : " + getPreparationTime() + " ms\n");
		sb.append("Verify time : " + getVerificationTime() + " ms\n");
		sb.append("VO size : " + getVOsize() + " B, " + getVOsize() / 1000.0 + " KB\n");
		return sb.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
