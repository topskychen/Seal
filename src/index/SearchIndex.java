/**
 * 
 */
package index;

import java.util.ArrayList;

import party.DataOwner;
import spatialindex.IShape;
import utility.VOCell;

/**
 * @author chenqian
 *
 */
public interface SearchIndex {

	public static enum INDEX_TYPE {BTree, RTree, QTree};

	public abstract ArrayList<VOCell> rangeQuery(IShape query);
	public abstract void buildIndex(ArrayList<DataOwner> owners, ArrayList<Entry> entries);
	public abstract INDEX_TYPE getType();

}
