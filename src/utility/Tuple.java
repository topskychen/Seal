/**
 * 
 */
package utility;

import index.Point;

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
public class Tuple implements RW{

	
	private Point[] point 	= null;
	private int[] 	tiStp	= null;
	
	public Tuple(Tuple tuple) {
		point = tuple.point;
		tiStp = tuple.tiStp;
	}
	
	/**
	 * Construct a tuple based on two tuples.
	 * The points tracks the bounds of two tuples.
	 * And the timeStp also are the bounds.
	 * @param t1
	 * @param t2
	 */
	public Tuple(Tuple t1, Tuple t2) {
		this.point = new Point[2];
		this.tiStp = new int[2];
		this.point[0] = t1.getLowPoint();
		this.point[1] = t2.getHiPoint();
		this.tiStp[0] = t1.getLowTiStp();
		this.tiStp[1] = t2.getHiTiStp();
	}
	
	/**
	 * Construct a tuple.
	 * @param v
	 * @param t
	 */
	public Tuple(int v, int t) {
		this.point = new Point[2];
		this.tiStp = new int[2];
		this.point[0] = new Point(v);
		this.point[1] = this.point[0];
		this.tiStp[0] = t;
		this.tiStp[1] = t;
	}
	
	/**
	 * 
	 */
	public Tuple() {
		// TODO Auto-generated constructor stub
	}

	public Point getLowPoint() {
		return point[0];
	}
	
	public int getLowTiStp() {
		return tiStp[0];
	}
	
	public Point getHiPoint() {
		return point[1];
	}
	
	public int getHiTiStp() {
		return tiStp[1];
	}
	
	public int getDim() {
		return point[0].getDim();
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
		int num = IO.readInt(ds);
		point = new Point[num];
		for (int i = 0; i < num; i ++) {
			point[i] = new Point(); 
			point[i].read(ds);
		}
		tiStp = IO.readIntArrays(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		IO.writeInt(ds, point.length);
		for (int i = 0; i < point.length; i ++) {
			point[i].write(ds);			
		}
		IO.writeIntArrays(ds, tiStp);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < point.length; i ++) {
			if (i != 0) sb.append(", ");
			sb.append(point[i].toString());
		}
		return sb.toString();
	}

}
