/**
 *
 */
package utility;

import index.MemQTree;
import index.MemRTree;
import index.Query.QUERY_TYPE;

import java.math.BigInteger;

import spatialindex.Region;
import timer.Timer;

/**
 * @author chenqian
 * 
 */
public class Global {

	public static enum OP {
		ADD, DEL
	}
	;

	public static enum MODE {
		LAZY, UPDATE, REBUILD, LOOSE
	}
	;

	
	/**
	 * TOTN differs from 10132, 30000, 100000, 300000, 1000000.
	 */
//	public static int				TOTN			= 10132;
	public static int				TOTN			= 10000;
//	public static int				TOTN			= 30000;
//	public static int				TOTN			= 100000;
//	public static int				TOTN			= 300000;
//	public static int				TOTN			= 1000000;
	
	public static int				D				= 4;
	public static int				L				= (int) (Math.log(TOTN) / Math.log(4)); // log_4(TOTN)
	public static int				THREAD_NUM		= 2;
	public static BigInteger		BITS24			= Utility.getBits1(24);
	public static BigInteger		BITS128			= Utility.getBits1(128);
	public static BigInteger		BITS152			= Utility.getBits1(152);
	public static BigInteger		BITS184			= Utility.getBits1(184);
	public static int				F				= 7;
	public static long				BOUND			= 100000;
	public static int				RANGE			= 100;
	public static Region			G_BOUND			= new Region(new double[] {
			0, 0									}, new double[] {
			BOUND + 1, BOUND + 1					});
	public static MemQTree			G_QTREE			= null;					// new
																				// MemQTree(4,
																				// G_BOUND,
																				// 0,
																				// 0);
	public static MemRTree			G_RTREE			= null;					// MemRTree.createTree();
	public static boolean			RT_VERBOSE		= false;
	public static int				PRINT_LIM		= 10;
	public static boolean 			IS_MULTI_THREAD = true;
	public static int				BUFFER_SIZE		= 150;
	public static MODE				G_MODE			= MODE.REBUILD;
	public static String			TEST_FILE_DIR	= "./data";
	public static int				TRA_LEN			= 2;
	// public static double[] RATIOS = {0.64};
	public static double[]			RATIOS			= {
													// 0.0025, 0.005, 0.01,
													0.02
													// , 0.04, 0.08, 0.16, 0.32,
													// 0.64
													};
	public static double[]			UPDATE_RTS		= { 0.1
													// , 0.2, 0.3, 0.4, 0.5
													};
	public static double			UPDATE_RT		= 0.3;
	public static Region			BOUNDS			= new Region(new double[] {
			116.282676, 39.827522					}, new double[] {
			116.477350, 39.994745					});
	public static int				QUERY_LIM		= 50;
	public static double			REGION_L		= 0;
	public static boolean			BATCH_QUERY		= true;
	public static boolean			DO_COST			= true;
	public static StatisticsDO		STAT_DO			= new StatisticsDO();
	public static boolean			INDEX_COST		= true;
	public static StatisticsIndex	STAT_INDEX		= new StatisticsIndex();
	public static Timer				G_TIMER			= null;
	public static QUERY_TYPE 		G_QUERY_TYPE	= QUERY_TYPE.range_query;
	public static int				G_K				= 256;
	public static int				G_Dim			= 2;

	
	static {
		double[] low = new double [Global.G_Dim];
		double[] high = new double [Global.G_Dim];
		for (int i = 0; i < Global.G_Dim; ++i) {
			low[i] = -1;
			high[i] = BOUND+1;
		}
		G_BOUND = new Region(low, high);
		L = (int) (Math.log(TOTN) / Math.log(1 << Global.G_Dim)); // log_4(TOTN)
		if (DO_COST || INDEX_COST)
			G_TIMER = new Timer();
		if (!System.getProperty("os.name").equals("Mac OS X")) {
			THREAD_NUM = 16;
		}
	}

	/**
     *
     */
	public Global() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
