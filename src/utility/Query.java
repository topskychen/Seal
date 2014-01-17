/**
 * 
 */
package utility;

import index.Point;

/**
 * @author chenqian
 *
 */
public class Query {

	Point low = null;
	Point high = null;
	
	/**
	 * Judge a point in the range or not.
	 * Support multi dimensions.
	 * @param point
	 * @return
	 */
	boolean inRange(Point L, Point H) {
		for (int i = 0; i < L.getDim(); i ++) {
			if (L.getCoord(i) > high.getCoord(i) || H.getCoord(i) < low.getCoord(i)) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Get the lower bound.
	 * @return
	 */
	public Point getLB() {
		return low;
	}
	
	/**
	 * Get the upper bound.
	 * @return
	 */
	public Point getHB() {
		return high;
	}
	
	/**
	 * Get the lower value, applied only to one d.
	 * @return
	 */
	public int getLowVal() {
		return low.getCoord(0);
	}
	
	/**
	 * Get the higher value, applied only to the one d.
	 * @return
	 */
	public int getHiVal() {
		return high.getCoord(0);
	}
	
	
	public Query(int low, int high) {
		this.low = new Point(low);
		this.high = new Point(high);
	}
	
	/**
	 * 
	 */
	public Query() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
