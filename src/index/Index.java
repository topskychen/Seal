/**
 * 
 */
package index;

import java.util.ArrayList;

import utility.Query;

/**
 * @author chenqian
 *
 */
public abstract class Index {

	/**
	 * 
	 */
	public Index() {
		// TODO Auto-generated constructor stub
	}

	public abstract ArrayList<Entry> RangeQuery(Query query);
	public abstract void buildIndex(ArrayList<Entry> entries);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
