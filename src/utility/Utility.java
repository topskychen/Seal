/**
 * 
 */
package utility;

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
	public static int comPre(int x, int y, int d) {
		long lx = x, ly = y, lz = 0; 
//		pi22(x);
//		pi22(y);
		for (int i = 0; i < 32 / d; i ++) {
			long mask = ((1L << (32 - d * i)) - 1) ^ ((1L << (32 - d - d * i)) - 1);
//			System.out.println(Long.toBinaryString(mask));
			if ((lx & mask) != (ly & mask)) {
				break;
			} else {
//				System.out.println(Long.toBinaryString(lx & mask));
				lz = lz | (lx & mask);
			}
		}
		return (int) lz;
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
		int z = comPre(61731, 61795, 4);
		System.out.println(Integer.toBinaryString(z));
	}

}
