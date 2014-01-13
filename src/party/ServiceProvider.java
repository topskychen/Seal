/**
 * 
 */
package party;

import java.util.ArrayList;

import index.Entry;
import index.Index;

/**
 * @author chenqian
 *
 */
public class ServiceProvider {

	Index index = null;
	
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

	}

}
