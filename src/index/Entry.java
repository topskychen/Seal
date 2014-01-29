/**
 * 
 */
package index;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

import party.TrustedRegister;
import index.SearchIndex.INDEX_TYPE;
import io.IO;
import io.RW;
import utility.Constants;
import utility.Seal;
import utility.Tuple;
import utility.Utility;

/**
 * @author chenqian
 *
 */
public class Entry implements RW{

	private Tuple 	tuple 	= null;
	private Seal 	seal 	= null;
	private int 	no		= 0;

	public Entry (Entry e) {
		this.tuple 	= new Tuple(e.getTuple());
		this.seal 	= new Seal(e.getSeal());
		this.no		= e.no;
	}
	
	/**
	 * Construct an entry based on two children.
	 * @param a
	 * @param b
	 */
	public Entry(Entry a, Entry b) {
		tuple = new Tuple(a.tuple, b.tuple);
		seal = new Seal(a.seal, b.seal);
		no = a.no + b.no;
	}
	
	public Entry(int id, Entry[] entries) {
		Tuple[] tuples = new Tuple[entries.length];
		Seal[]	seals = new Seal[entries.length];
		no = 0;
		for (int i = 0; i < entries.length; i ++) {
			tuples[i] = entries[i].getTuple();
			seals[i] = entries[i].getSeal();
			no += entries[i].no;
		}
		tuple = new Tuple(id, tuples);
		seal = new Seal(seals);
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



	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		tuple.write(ds);
		seal.write(ds);
		IO.writeInt(ds, no);
	}
	
	public Point getLB() {
		return tuple.getLowPoint();
	}
	
	public Point getHB() {
		return tuple.getHiPoint();
	}
	
	/**
	 * Get the lower value of the entry.
	 * This function called only the one-dimensional data.
	 * @return
	 */
	public int getLowVal() {
		return tuple.getLowPoint().getCoord(0);
	}
	
	/**
	 * Get the higher value of the entry.
	 * This function called only the one-dimensional data.
	 * @return
	 */
	public int getHiVal() {
		return tuple.getHiPoint().getCoord(0);
	}
	
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
}
