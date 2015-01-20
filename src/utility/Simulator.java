/**
 * 
 */
package utility;

import index.Query.QueryType;
import index.SearchIndex.INDEX_TYPE;

import java.util.List;

import party.Client;
import party.DataOwner;
import party.ServiceProvider;
import party.TrustedRegister;
import utility.EncFun.ENC_TYPE;
import utility.Global.MODE;

/**
 * @author chenqian
 * 
 */
public abstract class Simulator {

	TrustedRegister		ts					= null;
	List<DataOwner>		dataOwners			= null;
	ServiceProvider		serviceProvider		= null;
	Client				client				= null;
	double				preparationTime		= -1;
	double				verificationTime	= -1;
	long				voSize				= -1;
	StatisticsUpdate	statU				= new StatisticsUpdate();
	StatisticsQuery		statQ				= new StatisticsQuery();

	
	
	String		fileName	= Global.TEST_FILE_DIR + "/GO";
	INDEX_TYPE	indexType	= INDEX_TYPE.QTree;
	int			totN		= 1000;	// total DOs
	int			capacity	= 8;	// 1 << dim
	int			k			= 256; 	// kNN of k
	int			dim			= 3;	// dimension
	MODE		mode 		= MODE.UPDATE; // 
	int			bufferSize	= 150;
	double 		updateRate 	= 0.1;
	double		rteeRegionL	= 0;
	QueryType 	queryType 	= QueryType.range_query;
	double 		querySize 	= 0.01;
	boolean				recordIndex			= true;
	StatisticsIndex		STAT_INDEX			= null;
	
	public double getUpdateRate() {
		return updateRate;
	}
	
	public void setUpdateRate(double rate) {
		this.updateRate = rate;
	}
	
	public TrustedRegister getTrustedRegister() {
		return ts;
	}
	
	public double getRtreeRegionL() {
		return rteeRegionL;
	}
	
	public INDEX_TYPE getIndexType() {
		return indexType;
	}
	
	public MODE getMode() {
		return mode;
	}
	
	public int getDim() {
		return dim;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public int getTotalN() {
		return totN;
	}
	
	/**
	 * 
	 */
	public Simulator() {
		super();
		ts = TrustedRegister.getInstance(ENC_TYPE.Paillier, fileName);
		STAT_INDEX = new StatisticsIndex(indexType);
	}

	/**
	 * For initializing the keys and indexes.
	 */
	public abstract void init();

	/**
	 * Run multi times
	 */
	public abstract void run(int runId);

	/**
	 * Get the total time of simulation
	 * 
	 * @return
	 */
	public double getTotalSimulationTime() {
		return getPreparationTime() + getVerificationTime();
	}

	/**
	 * Get time of preparation
	 * 
	 * @return
	 */
	public double getPreparationTime() {
		if (preparationTime < 0) {
			throw new IllegalStateException(
					"The preparationTime is not set, maybe u need to call run function first");
		}
		return preparationTime;
	}

	/**
	 * Get time of verification
	 * 
	 * @return
	 */
	public double getVerificationTime() {
		if (verificationTime < 0) {
			throw new IllegalStateException(
					"The verificationTime time is not set, maybe u need to call run function first");
		}
		return verificationTime;
	}

	/**
	 * Get size of VO
	 * 
	 * @return
	 */
	public long getVOsize() {
		if (voSize < 0) {
			throw new IllegalStateException(
					"The voSize is not set, maybe u need to call run function first");
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
		sb.append("VO size : " + getVOsize() + " B, " + getVOsize() / 1000.0
				+ " KB\n");
		return sb.toString();
	}

	public void printStat() {
		System.out.println(statU);
		System.out.println(statQ);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public void clearStat() {
		if (statQ != null) {
			statQ.reSet();
		}
		if (statU != null) {
			statU.reSet();
		}
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public String getFileName() {
		return fileName;
	}

	public double getQuerySize() {
		return querySize;
	}

	public ServiceProvider getSP() {
		// TODO Auto-generated method stub
		return serviceProvider;
	}
	
	public StatisticsIndex getIndexStat() {
		return STAT_INDEX;
	}
	
	public boolean getRecordIndex() {
		return recordIndex;
	}

	public void setRecordIndex(boolean b) {
		recordIndex = false;
	}

}
