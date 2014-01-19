/**
 * 
 */
package index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import io.IO;
import io.RW;

/**
 * @author chenqian
 *
 */
public class Point implements RW{

	private int[] coords = null;
	
	/**
	 * Construct a point (one-dim).
	 * @param v
	 */
	public Point(int v) {
		coords = new int[1];
		coords[0] = v;
	}
	
	/**
	 * 
	 */
	public Point() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Get the dimension of a point.
	 * @return
	 */
	public int getDim() {
		return coords.length;
	}
	
	/**
	 * Get i^th coordinate of a point.
	 * @param i
	 * @return
	 */
	public int getCoord(int i) {
		return coords[i];
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void read(DataInputStream ds) {
		// TODO Auto-generated method stub
		coords = IO.readIntArrays(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		IO.writeIntArrays(ds, coords);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		for (int i = 0; i < getDim(); i ++) {
			if (i != 0) sb.append(", ");
			else sb.append(getCoord(i));
		}
		sb.append(']');
		return sb.toString();
	}

}
