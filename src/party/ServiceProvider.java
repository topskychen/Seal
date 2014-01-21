/**
 * 
 */
package party;

import java.util.ArrayList;

import utility.Query;
import utility.VO;
import index.BinarySearchTree;
import index.Entry;
import index.MemRTree;
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
		System.out.println("Index prepared!");
	}
	
	public VO rangeQuery(Query query) {
		VO vo = new VO();
		vo.prepare(index, query);
		return vo;
	}
	
	public void specifyIndex(INDEX_TYPE type) {
		if (type == INDEX_TYPE.BTree) {
			index = new BinarySearchTree(Entry.class);
		} else if (type == INDEX_TYPE.RTree) {
			index = MemRTree.createTree();
		} else if (type == INDEX_TYPE.QTree) {
			
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
