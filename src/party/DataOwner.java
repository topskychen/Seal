/**
 * 
 */
package party;

import index.Entry;

import java.util.ArrayList;
import java.util.Iterator;

import utility.Tuple;

/**
 * @author chenqian
 *
 */
public class DataOwner {

	private ArrayList<Entry> entries = null;
	private Iterator<Entry> iter = null;
	private byte[] secretShare = null;
	
	/**
	 * Prepare Seals.
	 */
	public void prepareSeals() {
		for (int i = 0; i < entries.size(); i ++) {
			entries.get(i).prepareSeal(secretShare);
		}
	}
	
	/**
	 * Reset the iterator of the entries to the begin.
	 */
	public void reSetIterator() {
		iter = entries.iterator();
	}
	
	/**
	 * Get the first entry from the data owner.
	 * @return
	 */
	public Entry getFirstEntry() {
		if (entries == null) {
			throw new NullPointerException("entries is null");
		}
		return entries.get(0);
	}
	
	/**
	 * Get the next entry from the data owner.
	 * @return
	 */
	public Entry getNextEntry() {
		if (iter == null) {
			throw new NullPointerException("entries is null");
		}
		return iter.next();
	}
	
	/**
	 * Append a value to the data owner.
	 * Remember to call the function prepareSeals afterward.
	 * @param p
	 */
	public void addValue(int v) {
		entries.add(new Entry(new Tuple(v, 0), null));
	}
	
	public void clear() {
		entries.clear();
		iter = null;
	}
	/**
	 * 
	 */
	public DataOwner(byte[] secretShare) {
		// TODO Auto-generated constructor stub
		this.entries = new ArrayList<Entry>();
		this.secretShare = secretShare;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
