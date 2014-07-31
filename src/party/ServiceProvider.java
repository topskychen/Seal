/**
 * 
 */
package party;

import index.BinarySearchTree;
import index.Entry;
import index.MemQTree;
import index.MemRTree;
import index.SearchIndex;
import index.SearchIndex.INDEX_TYPE;
import index.VO;
import io.IO;
import io.RW;

import java.util.ArrayList;

import spatialindex.IShape;
import timer.Timer;
import utility.Global;
import utility.Global.MODE;
import utility.StatisticsUpdate;


/**
 * @author chenqian
 * 
 */
public class ServiceProvider {

	SearchIndex			index	= null;
	Timer				timer	= null;
	StatisticsUpdate	statU	= null;

	/**
	 * Collect the data once, and build the index.
	 * 
	 * @param dataOwners
	 */
	public void collectDataOnce(ArrayList<DataOwner> dataOwners,
			INDEX_TYPE type, int runId) {
		if (Global.G_MODE != MODE.REBUILD) {
			if (index == null) {
				if (type == INDEX_TYPE.BTree) {
					index = new BinarySearchTree(Entry.class);
				} else if (type == INDEX_TYPE.RTree) {
					index = Global.G_RTREE;
				} else if (type == INDEX_TYPE.QTree) {
					index = Global.G_QTREE;
				}
			}
		} else {
			specifyIndex(type);
		}
		timer.reset();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		for (int i = 0; i < dataOwners.size(); i++) {
			DataOwner owner = dataOwners.get(i);
			Entry entry = owner.getEntry(runId);
			if (entry != null)
				entries.add(entry);
			else {
				// System.out.println("do: " + i);
			}
		}
		index.buildIndex(dataOwners, entries, statU);
		timer.stop();
		if (Global.INDEX_COST) {
			Global.STAT_INDEX.append(timer.timeElapseinMs(),
					IO.toBytes((RW) index).length);
		}
		if (!Global.BATCH_QUERY) {
			System.out.println("Index prepared! consumes: "
					+ timer.timeElapseinMs() + " ms");
		}
	}

	public VO rangeQuery(IShape query, int runId) {
		VO vo = new VO(runId);
		vo.prepare(index, query);
		return vo;
	}
	
	public ArrayList<IShape> kNN(IShape query, int k) {
		return index.kNN(query, k);
	}
	
	public ArrayList<IShape> skyline() {
		return index.skyline();
	}

	public void specifyIndex(INDEX_TYPE type) {
		if (type == INDEX_TYPE.BTree) {
			index = new BinarySearchTree(Entry.class);
		} else if (type == INDEX_TYPE.RTree) {
			index = MemRTree.createTree(Global.G_Dim);
		} else if (type == INDEX_TYPE.QTree) {
			index = new MemQTree(Global.G_Dim, 1 << Global.G_Dim, Global.G_BOUND, 0, 0);
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
		// System.out.println(MOD.toString(2));
	}

}
