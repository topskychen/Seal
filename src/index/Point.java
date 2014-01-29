/**
 * 
 */
package index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import math.MathUtility;
import io.IO;
import io.RW;

/**
 * @author chenqian
 *
 */
public class Point implements RW{

	private int[] coords = null;
	
	public Point(Point p) {
		coords = new int[p.getDim()];
		for (int i = 0; i < coords.length; i ++) {
			coords[i] = p.getCoord(i);
		}
	}
	
	/**
	 * Construct a point (one-dim).
	 * @param v
	 */
	public Point(int v) {
		coords = new int[1];
		coords[0] = v;
	}
	
	/**
	 * Construct a point (two-dim).
	 * @param x
	 * @param y
	 */
	public Point(int x, int y) {
		coords = new int[2];
		coords[0] = x;
		coords[1] = y;
	}
	
	/**
	 * Construct a point (multi-dim).
	 * @param coords
	 */
	public Point(int[] coords) {
		this.coords = coords;
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
	 * Set the coord i as v.
	 * @param i
	 * @param v
	 */
	public void setCoord(int i, int v) {
		coords[i] = v;
	}
	
	public double[] doubleCoords() {
		double[] p = new double[getDim()];
		for (int i = 0; i < getDim(); i ++) {
			p[i] = getCoord(i);
		}
		return p;
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
			sb.append(getCoord(i));
		}
		sb.append(']');
		return sb.toString();
	}
	
	/**
	 * Get the lower point by considering both p1 and p2.
	 * i.e. min(p1.x, p2.x) ...
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Point lower(Point p1, Point p2) {
		if (p1.getDim() != p2.getDim()) {
			throw new IllegalStateException("p1 has different dimensions with p2.");
		}
		int[] coords = new int[p1.getDim()];
		for (int i = 0; i < p1.getDim(); i ++) {
			coords[i] = Math.min(p1.getCoord(i), p2.getCoord(i));
		}
		return new Point(coords);
	}
	
	/**
	 * Get the larger point by considering both the p1 and p2.
	 * i.e., max(p1.x, p2.x) ...
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Point larger(Point p1, Point p2) {
		if (p1.getDim() != p2.getDim()) {
			throw new IllegalStateException("p1 has different dimensions with p2.");
		}
		int[] coords = new int[p1.getDim()];
		for (int i = 0; i < p1.getDim(); i ++) {
			coords[i] = Math.max(p1.getCoord(i), p2.getCoord(i));
		}
		return new Point(coords);
	}
	
	public boolean equals(Point q) {
		if (getDim() != q.getDim()) return false;
		for (int i = 0; i < coords.length; i ++) {
			if (MathUtility.D(coords[i] - q.getCoord(i)) != 0) return false; 
		}
		return true;
	}

}
