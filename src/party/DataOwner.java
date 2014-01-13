/**
 * 
 */
package party;

import index.Entry;

import java.util.ArrayList;
import java.util.Iterator;

import utility.Seal;
import utility.Tuple;

/**
 * @author chenqian
 *
 */
public class DataOwner {

	private ArrayList<Entry> entries = null;
	private Iterator<Entry> iter = null;
	
	public void prepareSeals() {
		for (int i = 0; i < entries.size(); i ++) {
			entries.get(i).prepareSeal();
		}
	}
	
	public void reSetIterator() {
		iter = entries.iterator();
	}
	
	public Entry getFirstEntry() {
		if (entries == null) {
			throw new NullPointerException("tuples is null");
		}
		return entries.get(0);
	}
	
	public Entry getNextEntry() {
		if (iter == null) {
			throw new NullPointerException("tuples is null");
		}
		return iter.next();
	}
	
	/**
	 * 
	 */
	public DataOwner() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
