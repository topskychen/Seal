/**
 * 
 */
package utility;

import index.Point;
import index.SearchIndex.INDEX_TYPE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;


import io.IO;
import io.RW;

/**
 * @author chenqian
 *
 */
public class Tuple implements RW{

	private int		id 		= -1;
	private Point	point 	= null;
	private int 	tiStp	= -1;
	private int[] 	comPre	= null;
	
	public Tuple(int id, Point point, int tiStp, int[] comPre) {
		this.id 	= id;
		this.point 	= point;
		this.tiStp 	= tiStp;
		this.comPre = comPre;
	}
	
	
	/**
	 * Construct a tuple based on two tuples.
	 * The points tracks the bounds of two tuples.
	 * And the timeStp also are the bounds.
	 * lev is 0-based.
	 * @param t1
	 * @param t2
	 * @param lev
	 */
	public Tuple(Tuple t1, Tuple t2, int lev) {
		if (lev == -1 || t1.getComPre().length < lev + 1 || t2.getComPre().length < lev + 1) {
			this.comPre = Utility.comPre(t1.getComPre(), t2.getComPre());
		} else {
			this.comPre = new int[lev + 1];
			System.arraycopy(t1.getComPre(), 0, comPre, 0, lev + 1);
		}
	}
	
	/**
	 * Construct a tuple based on multi tuples.
	 * @param tuples
	 */
	public Tuple(int id, Tuple[] tuples, int lev) {
		this.id = id;
		Tuple tuple = tuples[0];
		for (int i = 1; i < tuples.length; i ++) {
			tuple = new Tuple(tuple, tuples[i], lev);
		}
		comPre 	= tuple.comPre;
	}
	
	/**
	 * Construct a tuple.
	 * @param v
	 * @param t
	 */
	public Tuple(int id, Point p, int t, int[] comPre, INDEX_TYPE type) {
		this.id = id;
		this.point = p;
		this.tiStp = t;
//		this.point[0] = p;
//		this.point[1] = this.point[0];
//		this.tiStp[0] = t;
//		this.tiStp[1] = t;
		if (type == INDEX_TYPE.BTree) {
			int value = p.getCoord(0);
			this.comPre = new int[Constants.L];
			for (int i = 0; i < Constants.L; i ++) {
				int v = (value >> (Constants.D * i));
				this.comPre[Constants.L - i - 1] = v;
			}
		} else if (type == INDEX_TYPE.RTree) {
			this.comPre = comPre;
		} else if (type == INDEX_TYPE.QTree) {
			ArrayList<Integer> ids = Constants.G_QTREE.getPath(new spatialindex.Point(p.doubleCoords()));
			this.comPre = new int[Constants.L];
			for (int i = 0; i < Constants.L; i ++) {
				this.comPre[i] = ids.get(i);
			}
		} else {
			throw new IllegalStateException("No such index!");
		}
	}
	
	/**
	 * 
	 */
	public Tuple() {
		// TODO Auto-generated constructor stub
	}

	public Point getPoint() {
		return point;
	}
	
	public int[] getComPre() {
		return comPre;
	}
	
	public int getId() {
		return id;
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
		id = IO.readInt(ds);
		if (IO.readBoolean(ds)) {
			point = new Point();
			point.read(ds);
		}
		tiStp = IO.readInt(ds);
		comPre = IO.readIntArrays(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		IO.writeInt(ds, id);
		if (point == null) {
			IO.writeBoolean(ds, false);
		} else {
			IO.writeBoolean(ds, true);
			point.write(ds);
		}
		IO.writeInt(ds, tiStp);
		IO.writeIntArrays(ds, comPre);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("@" + id + "\n");
		if (point != null) {
			point.toString();
		}
		sb.append("\n[");
		for (int i = 0; i < comPre.length; i ++) {
			if (i != 0) sb.append(", ");
			sb.append(comPre[i]);
		}
		sb.append("]");
		return sb.toString();
	}
	
	public int getTS() {
		return tiStp;
	}

	public Tuple clone() {
		int[] newComPre	= new int[comPre.length];
		System.arraycopy(comPre, 0, newComPre, 0, comPre.length);
		return new Tuple(id, point.clone(), tiStp, newComPre);
	}
}
