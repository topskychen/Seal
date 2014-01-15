/**
 * 
 */
package index;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import io.RW;
import utility.Seal;
import utility.Tuple;

/**
 * @author chenqian
 *
 */
public class Entry implements RW{

	private Tuple tuple = null;
	private Seal seal = null;

	/**
	 * Construct an entry based on two children.
	 * @param a
	 * @param b
	 */
	public Entry(Entry a, Entry b) {
		tuple = new Tuple(a.tuple, b.tuple);
		seal = new Seal(a.seal, b.seal);
	}
	
	/**
	 * Construct an entry.
	 * @param tuple
	 * @param seal
	 */
	public Entry(Tuple tuple, Seal seal) {
		this.tuple = tuple;
		this.seal = seal;
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
	public void prepareSeal(byte[] secretShare) {
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
	}



	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		tuple.write(ds);
		seal.write(ds);
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
