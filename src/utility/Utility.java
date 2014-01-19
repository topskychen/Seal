/**
 * 
 */
package utility;

import io.IO;

import java.math.BigInteger;

/**
 * @author chenqian
 *
 */
public class Utility {

	/**
	 * Print a integer as a binary string.
	 * @param i
	 */
	public static void pi22(int i) {
		System.out.println(Integer.toBinaryString(i));
	}
	
	/**
	 * Print a long as a binary string.
	 * @param i
	 */
	public static void pl22(int i) {
		System.out.println(Integer.toBinaryString(i));
	}
	
	/**
	 * Get the common prefix of two integers.
	 * @param x
	 * @param y
	 * @param d
	 * @return
	 */
	public static int[] comPre(int[] x, int[] y, int d) { 
//		pi22(x);
//		pi22(y);
		int i, z = 0, b = Math.min(x[1], y[1]);
		for (i = 0; i < b + 1; i ++) {
//			System.out.println(Long.toBinaryString(mask));
			int shiftx = (x[1] - i);
			int shifty = (y[1] - i);
			if ((x[0] >> (shiftx * d)) != (y[0] >> (shifty * d))) {
				break;
			} else {
//				System.out.println(Long.toBinaryString(lx & mask));
				z = x[0] >> (shiftx * d);
			}
		}
		return new int[]{z, i - 1};
	}
	
	/**
	 * Get the corresponding bigInteger based on the byte[].
	 * @param bytes
	 * @return
	 */
	public static BigInteger getBI(byte[] bytes) {
		return new BigInteger(IO.toHexFromBytes(bytes), 16);
	}
	
	public static BigInteger getBits1(int d) {
		return BigInteger.ONE.shiftLeft(d).subtract(BigInteger.ONE);
	}
	/**
	 * 
	 */
	public Utility() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(getBits1(24).toString(2));
	}

}
