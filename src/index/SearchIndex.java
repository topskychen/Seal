/**
 * 
 */
package index;

import java.util.ArrayList;

import spatialindex.IShape;
import utility.StatisticsUpdate;

/**
 * @author chenqian
 *
 */
public interface SearchIndex {

	public static enum INDEX_TYPE {BTree, RTree, QTree};

	public abstract ArrayList<VOCell> rangeQuery(IShape query);
	public abstract ArrayList<IShape> kNN(IShape query, int k);
	public abstract ArrayList<IShape> skyline();
//	public abstract 
	public abstract void updateIndex(ArrayList<Entry> entries, StatisticsUpdate statU);
	public abstract byte[] toBytes();
	public abstract int[] getPrefix(IShape point);
	public abstract INDEX_TYPE getType();

}
