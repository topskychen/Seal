/**
 * 
 */
package party;

import java.math.BigInteger;
import java.util.ArrayList;

import index.Entry;
import index.SearchIndex;

/**
 * @author chenqian
 *
 */
public class ServiceProvider {

	SearchIndex index = null;
	
	/**
	 * Collect the data once, and build the index.
	 * @param dataOwners
	 */
	public void collectDataOnce(ArrayList<DataOwner> dataOwners) {
		ArrayList<Entry> entries = new ArrayList<Entry>();
		for (int i = 0; i < dataOwners.size(); i ++) {
			entries.add(dataOwners.get(i).getFirstEntry());
		}
		index.buildIndex(entries);
	}
	
	/**
	 * 
	 */
	public ServiceProvider() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		System.out.println(MOD.toString(2));
	}

}
