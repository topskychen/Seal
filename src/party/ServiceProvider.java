/**
 * 
 */
package party;

import java.util.ArrayList;

import spatialindex.IShape;
import timer.Timer;
import utility.Constants;
import utility.StatisticsQuery;
import utility.StatisticsUpdate;
import utility.VO;
import utility.Constants.MODE;
import index.BinarySearchTree;
import index.Entry;
import index.MemQTree;
import index.MemRTree;
import index.SearchIndex;
import index.SearchIndex.INDEX_TYPE;

/**
 * @author chenqian
 *
 */
public class ServiceProvider {

	SearchIndex 		index = null;
	Timer 				timer = null;
	StatisticsUpdate 	statU = null;
	
	/**
	 * Collect the data once, and build the index.
	 * @param dataOwners
	 */
	public void collectDataOnce(ArrayList<DataOwner> dataOwners, INDEX_TYPE type, int runId) {
		if (Constants.G_MODE != MODE.REBUILD) {
			if (index == null) {
				if (type == INDEX_TYPE.BTree) {
					index = new BinarySearchTree(Entry.class);
				} else if (type == INDEX_TYPE.RTree) {
					index = Constants.G_RTREE;
				} else if (type == INDEX_TYPE.QTree) {
					index = Constants.G_QTREE;
				}			
			}
		} else {			
			specifyIndex(type);
		}
		timer.reset();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		for (int i = 0; i < dataOwners.size(); i ++) {
			Entry entry = dataOwners.get(i).getEntry(runId);
			if (entry != null) 
				entries.add(entry);
			else {
//				System.out.println("do: " + i);
			}
		}
		index.buildIndex(dataOwners, entries, statU);
		timer.stop();
		System.out.println("Index prepared! consumes: " + timer.timeElapseinMs() + " ms");
	}
	
	public VO rangeQuery(IShape query, int runId) {
		VO vo = new VO(runId);
		vo.prepare(index, query);
		return vo;
	}
	
	public void specifyIndex(INDEX_TYPE type) {
		if (type == INDEX_TYPE.BTree) {
			index = new BinarySearchTree(Entry.class);
		} else if (type == INDEX_TYPE.RTree) {
			index = MemRTree.createTree();
		} else if (type == INDEX_TYPE.QTree) {
			index = new MemQTree(4, Constants.G_BOUND, 0, 0);
		}
	}
	
	
	/**
	 * 
	 */
	public ServiceProvider(StatisticsUpdate statU) {
		// TODO Auto-generated constructor stub
		this.timer = new Timer();
		this.statU = statU;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		System.out.println(MOD.toString(2));
	}

}
