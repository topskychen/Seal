/**
 * 
 */
package index;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

import io.IO;
import io.RW;
import spatialindex.IShape;
import utility.Seal;
import utility.Tuple;

/**
 * @author chenqian
 *
 */
public class Entry implements RW{

	private Tuple 	tuple 	= null;
	private Seal 	seal 	= null;
	private int 	no		= 0;

	public Entry(Tuple tuple, Seal seal, int no) {
		this.tuple 	= tuple;
		this.seal	= seal;
		this.no 	= no;
	}
	
	public Entry (Entry e) {
		this.tuple 	= e.getTuple();
		this.seal 	= e.getSeal();
		this.no		= e.no;
	}
	
	/**
	 * Construct an entry based on two children.
	 * @param a
	 * @param b
	 */
	public Entry(Entry a, Entry b, int lev) {
		tuple = new Tuple(a.tuple, b.tuple, lev);
		seal = new Seal(a.seal, b.seal, false);
		no = a.no + b.no;
	}
	
	public Entry(int id, Entry[] entries, int lev) {
		Tuple[] tuples = new Tuple[entries.length];
		Seal[]	seals = new Seal[entries.length];
		no = 0;
		for (int i = 0; i < entries.length; i ++) {
			tuples[i] = entries[i].getTuple();
			seals[i] = entries[i].getSeal();
			no += entries[i].no;
		}
		tuple = new Tuple(id, tuples, lev);
		seal = new Seal(seals);
	}
	
	public void update(Entry oldEntry, Entry newEntry) {
		if (oldEntry == null) {
			seal = new Seal(seal, newEntry.seal, false);
			tuple = new Tuple(tuple, newEntry.getTuple(), -1);
			no ++;
		} else if (newEntry == null) {
			seal = new Seal(seal, oldEntry.seal, true);
			no --;
		} else if (oldEntry != null && newEntry != null) {
			seal = new Seal(seal, oldEntry.seal, true);
			tuple = new Tuple(tuple, newEntry.getTuple(), -1);
			seal = new Seal(seal, newEntry.seal, false);
		} else {
			throw new IllegalStateException("The old/new entry is illegal.");
		}
	}
	
	/**
	 * Construct an entry for one dim.
	 * @param tuple
	 * @param seal
	 */
	public Entry(Tuple tuple, Seal seal) {
		this.tuple 	= tuple;
		this.seal 	= seal;
		this.no 	= 1;
	}
	
	/**
	 * Return the number of points in this entry.
	 * @return
	 */
	public int getNO() {
		return no;
	}
	
	/**
	 * Get the tuple of the entry.
	 * @return
	 */
	public Tuple getTuple() {
		return tuple;
	}

	/**
	 * Set the tuple of the entry.
	 * @param tuple
	 */
	public void setTuple(Tuple tuple) {
		this.tuple = tuple;
	}

	/**
	 * Get the seal.
	 * @return
	 */
	public Seal getSeal() {
		return seal;
	}

	/**
	 * Set the seal.
	 * @param seal
	 */
	public void setSeal(Seal seal) {
		this.seal = seal;
	}

	/**
	 * Prepare a seal based on the tuple and the secretShare.
	 * @param secretShare
	 */
	public void prepareSeal(BigInteger secretShare) {
		seal = new Seal(tuple, secretShare);
	}

	/**
	 * 
	 */
	public Entry() {
		// TODO Auto-generated constructor stub
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
		tuple = new Tuple();
		tuple.read(ds);
		seal = new Seal();
		seal.read(ds);
		no = IO.readInt(ds);
	}


	public IShape getShape() {
		return tuple.getShape();
	}
	
	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		tuple.write(ds);
		seal.write(ds);
		IO.writeInt(ds, no);
	}
	
//	/**
//	 * Get the lower value of the entry.
//	 * This function called only the one-dimensional data.
//	 * @return
//	 */
//	public int getLowVal() {
//		return tuple.getPoint().getCoord(0);
//	}
//	
//	/**
//	 * Get the higher value of the entry.
//	 * This function called only the one-dimensional data.
//	 * @return
//	 */
//	public int getHiVal() {
//		return tuple.getPoint().getCoord(0);
//	}
//	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("tuple : " + tuple + "\n");
		sb.append("seal : " + seal + "\n");
		sb.append("no : " + no + "\n");
		return sb.toString();
	}
	
	public int getId() {
		return tuple.getId();
	}
	
	public int getTS() {
		return tuple.getTS();
	}
	
	public Entry clone() {
		return new Entry(tuple.clone(), seal.clone(), no);
	} 
}
