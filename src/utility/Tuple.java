/**
 * 
 */
package utility;

import index.SearchIndex.INDEX_TYPE;
import io.IO;
import io.RW;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import spatialindex.IShape;
import spatialindex.Point;
import spatialindex.Region;

/**
 * @author chenqian
 * 
 */
public class Tuple implements RW {

	private int		id		= -1;
	private IShape	shape	= null;
	private int		tiStp	= -1;
	private int[]	comPre	= null;

	/**
	 * Construct a tuple based on two tuples. The points tracks the bounds of
	 * two tuples. And the timeStp also are the bounds. lev is 0-based.
	 * 
	 * @param t1
	 * @param t2
	 * @param lev
	 */
	public Tuple(Tuple t1, Tuple t2, int lev) {
		if (lev == -1 || t1.getComPre().length < lev + 1
				|| t2.getComPre().length < lev + 1) {
			this.comPre = Utility.comPre(t1.getComPre(), t2.getComPre());
		} else {
			this.comPre = new int[lev + 1];
			System.arraycopy(t1.getComPre(), 0, comPre, 0, lev + 1);
		}
	}

	/**
	 * Construct a tuple based on multi tuples.
	 * 
	 * @param tuples
	 */
	public Tuple(int id, Tuple[] tuples, int lev) {
		this.id = id;
		Tuple tuple = tuples[0];
		for (int i = 1; i < tuples.length; i++) {
			tuple = new Tuple(tuple, tuples[i], lev);
		}
		comPre = tuple.comPre;
	}

	/**
	 * Construct a tuple.
	 * 
	 * @param v
	 * @param t
	 */
	public Tuple(int id, IShape p, int t, int[] comPre, INDEX_TYPE type) {
		this.id = id;
		this.shape = p;
		this.tiStp = t;
		// this.point[0] = p;
		// this.point[1] = this.point[0];
		// this.tiStp[0] = t;
		// this.tiStp[1] = t;
		if (type == INDEX_TYPE.BTree) {
			int value = (int) ((Point) p).getCoord(0);
			this.comPre = new int[Global.L];
			for (int i = 0; i < Global.L; i++) {
				int v = (value >> (Global.D * i));
				this.comPre[Global.L - i - 1] = v;
			}
		} else if (type == INDEX_TYPE.RTree) {
			this.comPre = comPre;
		} else if (type == INDEX_TYPE.QTree) {
			ArrayList<Integer> ids = Global.G_QTREE.getPath((Point) p);
			this.comPre = new int[Global.L];
			for (int i = 0; i < Global.L; i++) {
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

	public IShape getShape() {
		return shape;
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
		int type = IO.readInt(ds);
		if (type != 0) {
			if (type == 1) {
				shape = new Point();
				((Point) shape).read(ds);
			} else {
				shape = new Region();
				((Region) shape).read(ds);
			}
		}
		tiStp = IO.readInt(ds);
		comPre = IO.readIntArrays(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		IO.writeInt(ds, id);
		if (shape == null) {
			IO.writeInt(ds, 0);
		} else {
			if (shape instanceof Point) {
				IO.writeInt(ds, 1);
				((Point) shape).write(ds);
			} else {
				IO.writeInt(ds, 2);
				((Region) shape).write(ds);
			}
		}
		IO.writeInt(ds, tiStp);
		if (comPre != null)
			IO.writeIntArrays(ds, comPre);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("@" + id + "\n");
		if (shape != null) {
			shape.toString();
		}
		sb.append("\n[");
		for (int i = 0; i < comPre.length; i++) {
			if (i != 0)
				sb.append(", ");
			sb.append(comPre[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	public int getTS() {
		return tiStp;
	}

	public Tuple clone() {
		Tuple tuple = new Tuple();
		tuple.id = id;
		tuple.shape = shape.clone();
		tuple.comPre = new int[comPre.length];
		System.arraycopy(comPre, 0, tuple.comPre, 0, comPre.length);
		tuple.tiStp = tiStp;
		return tuple;
	}

	public void setShape(IShape shape) {
		this.shape = shape;
	}

	public void setTS(int tiStp) {
		this.tiStp = tiStp;
	}

	public void setComPre(int[] comPre) {
		this.comPre = new int[comPre.length];
		System.arraycopy(comPre, 0, this.comPre, 0, comPre.length);
	}
}
