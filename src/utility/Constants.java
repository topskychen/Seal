/**
 * 
 */
package utility;

import index.MemQTree;

import java.math.BigInteger;

import spatialindex.Region;

/**
 * @author chenqian
 *
 */
public class Constants {
	public static int 			D 			= 4;
	public static int 			L 			= 6;
	public static int 			THREAD_NUM 	= 4;
	public static BigInteger 	BITS24 		= Utility.getBits1(24);
	public static BigInteger 	BITS128 	= Utility.getBits1(128);
	public static BigInteger 	BITS152 	= Utility.getBits1(152);
	public static BigInteger 	BITS184 	= Utility.getBits1(184);
	public static int			F			= 5;
	public static int 			BOUND		= 10000;
	public static Region 		G_BOUND		= new Region(new double[] {0, 0}, new double[] {BOUND, BOUND});
	public static MemQTree		G_QTREE		= new MemQTree(4, G_BOUND);
	
			
	/**
	 * 
	 */
	public Constants() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
