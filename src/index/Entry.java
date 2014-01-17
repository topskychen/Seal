/**
 * 
 */
package index;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

import io.IO;
import io.RW;
import utility.Seal;
import utility.Tuple;

/**
 * @author chenqian
 *
 */
public class Entry implements RW{

	private int		id 		= -1;
	private Tuple 	tuple 	= null;
	private Seal 	seal 	= null;
	private int 	no		= 0;

	public Entry (Entry e) {
		this.id 	= e.id;
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
	
	/**
	 * Construct an entry.
	 * @param tuple
	 * @param seal
	 */
	public Entry(int id, Tuple tuple, Seal seal) {
		this.id 	= id;
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
	
	public int getId() {
		return id;
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
	public Entry(int id) {
		this.id = id;
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
		id = IO.readInt(ds);
		tuple = new Tuple();
		tuple.read(ds);
		seal = new Seal();
		seal.read(ds);
		no = IO.readInt(ds);
	}



	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		IO.writeInt(ds, id);
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
}
