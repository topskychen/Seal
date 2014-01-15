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
public interface SearchIndex {


	public abstract ArrayList<Entry> rangeQuery(Query query);
	public abstract void buildIndex(ArrayList<Entry> entries);

}
