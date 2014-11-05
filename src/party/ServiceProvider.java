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

import java.util.ArrayList;

import spatialindex.IShape;
import timer.Timer;
import utility.Simulator;
import utility.StatisticsUpdate;


/**
 * @author chenqian
 * 
 */
public class ServiceProvider {

	SearchIndex			index		= null;
	Timer				timer		= null;
	StatisticsUpdate	statU		= null;
	Simulator			simulator 	= null;
	
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

	public void updateIndex(ArrayList<Entry> entries) {
		index.updateIndex(entries, statU);
	}
	
	//TODO 
	public void init() {
		
	}
	
	public int getIndexSize() {
		return index.toBytes().length;
	}
	
	public void specifyIndex(INDEX_TYPE type) {
		if (type == INDEX_TYPE.BTree) {
			index = new BinarySearchTree(Entry.class);
		} else if (type == INDEX_TYPE.RTree) {
			index = MemRTree.createTree(simulator);
		} else if (type == INDEX_TYPE.QTree) {
			index = MemQTree.createTree(simulator);
		}
	}

	/**
	 * 
	 */
	public ServiceProvider(StatisticsUpdate statU, Simulator sim) {
		// TODO Auto-generated constructor stub
		this.timer = new Timer();
		this.statU = statU;
		this.simulator = sim;
		specifyIndex(sim.getIndexType());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// System.out.println(MOD.toString(2));
	}

}
