/**
 * 
 */
package index;

import java.util.ArrayList;

import utility.Query;
import utility.VOCell;

/**
 * @author chenqian
 *
 */
public interface SearchIndex {

	public static enum INDEX_TYPE {BTree, Rtree, Qtree};

	public abstract ArrayList<VOCell> rangeQuery(Query query);
	public abstract void buildIndex(ArrayList<Entry> entries);

}
