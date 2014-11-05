/**
 *
 */
package utility;

import java.math.BigInteger;

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

	public static int 				FANOUT			= 10;
	public static int				L 				= 6;
	public static int				THREAD_NUM		= 2; 
	public static BigInteger		BITS24			= Utility.getBits1(24);
	public static BigInteger		BITS128			= Utility.getBits1(128);
	public static BigInteger		BITS152			= Utility.getBits1(152);
	public static BigInteger		BITS184			= Utility.getBits1(184);
	public static int				F				= 10; 		//fanout of rtree
	public static long				BOUND			= 100000; 	// bound of data
	
	public static boolean			RT_VERBOSE		= false;
	public static int				PRINT_LIM		= 10;
	
	public static String			TEST_FILE_DIR	= "./data";
	public static double[]			QUERY_SIZES			= {0.01, 0.02};
//	public static double[]			QUERY_SIZES			= {
//													 0.00025, 0.0005, 0.001,
//													0.002, 0.004, 0.008, 0.016
//													// , 0.04, 0.08, 0.16, 0.32,
//													// 0.64
//													};
	public static double[]			UPDATE_RATES		= { 0.1, 0.2, 0.3, 0.4, 0.5 };
//	public static double[]			UPDATE_RATES		= { 0.001, 0.002, 0.003, 0.004, 0.005 };
	public static int				RUN_TIMES		= 20;
	
	public static Timer				TIMER			= null;
	

	
	static {
		TIMER = new Timer();
		if (!System.getProperty("os.name").equals("Mac OS X")) {
			THREAD_NUM = 16;
			QUERY_SIZES	= new double[] {
					 0.00025, 0.0005, 0.001,
					0.002, 0.004, 0.008, 0.016
					};
			UPDATE_RATES= new double[] { 0.001, 0.002, 0.003, 0.004, 0.005 };
		}
	}

	/**
     *
     */
	public Global() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
