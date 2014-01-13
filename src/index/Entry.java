/**
 * 
 */
package index;

import utility.Seal;
import utility.Tuple;

/**
 * @author chenqian
 *
 */
public class Entry {

	private Tuple tuple = null;
	private Seal seal = null;
	
	public Entry(Tuple tuple, Seal seal) {
		this.tuple = tuple;
		this.seal = seal;
	}
	
	
	
	public Tuple getTuple() {
		return tuple;
	}



	public void setTuple(Tuple tuple) {
		this.tuple = tuple;
	}



	public Seal getSeal() {
		return seal;
	}



	public void setSeal(Seal seal) {
		this.seal = seal;
	}


	public void prepareSeal() {
		seal = new Seal(tuple);
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

}
