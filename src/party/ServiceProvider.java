/**
 * 
 */
package party;

import java.math.BigInteger;
import java.util.ArrayList;

import memoryindex.BinaryTree;
import memoryindex.IQueryStrategy;
import utility.Query;
import utility.VO;
import utility.VOCell;
import index.BinarySearchTree;
import index.Entry;
import index.RetrieveStrategy;
import index.SearchIndex;
import index.SearchIndex.INDEX_TYPE;

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
	
	public VO rangeQuery(Query query) {
		VO vo = new VO();
		vo.prepare(index, query);
		return vo;
	}
	
	public void specifyIndex(INDEX_TYPE type) {
		if (type == INDEX_TYPE.BTree) {
			index = new BinarySearchTree(Entry.class);
		}
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
