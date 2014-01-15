/**
 * 
 */
package index;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import utility.Query;
import memoryindex.BinaryTree;
import memoryindex.IQueryStrategy;
import multithread.MultiThread;
import multithread.Task;

/**
 * @author chenqian
 *
 */
public class BinarySearchTree extends BinaryTree implements SearchIndex {

	public DataOutputStream ds = null;

	/**
	 * Construction needs the specification of class of value.
	 * @param classValue
	 */
	public BinarySearchTree(Class classValue) {
		super(classValue);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */


	/* (non-Javadoc)
	 * @see index.SearchIndex#rangeQuery(index.Query)
	 */
	@Override
	public ArrayList<Entry> rangeQuery(Query query) {
		// TODO Auto-generated method stub
		RangeQueryStrategy rangeQueryStrategy = new RangeQueryStrategy(query.getLB().getCoord(0), query.getHB().getCoord(0));
		queryStrategy(rangeQueryStrategy);
		return rangeQueryStrategy.getResults();
	}

	/* (non-Javadoc)
	 * @see index.SearchIndex#buildIndex(index.Trajectory, crypto.PMAC)
	 */
	@Override
	public void buildIndex(ArrayList<Entry> entries) {
		// TODO Auto-generated method stub
		
		BinaryTree[] nodes = new BinaryTree[entries.size()]; int size = 0;
		for (int i = 1; i <= entries.size(); i ++) {
			nodes[size ++] = new BinaryTree<Integer, Entry>(i, entries.get(i), getClassValue());
		}
		buildIndex(nodes, size);
		flush();
	}
	
	/**
	 * Flush to file
	 */
	public void flush() {
		try {
			write(ds);
			ds.flush();
			ds.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

		
	class RangeQueryStrategy implements IQueryStrategy {

		private ArrayList<BinaryTree> toVisit = new ArrayList<BinaryTree>();
		private ArrayList<Entry> results = new ArrayList<Entry>();
		private int lBound, rBound;
		
		
		public RangeQueryStrategy(int lBound, int rBound) {
			super();
			this.lBound = lBound;
			this.rBound = rBound;
		}

		public ArrayList<Entry> getResults() {
			return results;
		}

		@Override
		public void getNextEntry(BinaryTree n, BinaryTree[] next,
				boolean[] hasNext) {
			// TODO Auto-generated method stub
			if (!n.isLeftChildEmpty()) {
				Entry data = (Entry)n.getLeftChild().getValue();
				if (visitData(data)) {
					toVisit.add(n.getLeftChild());
				}
			}
			if (!n.isRightChildEmpty()) {
				Entry data = (Entry) n.getRightChild().getValue();
				if (visitData(data)) {
					toVisit.add(n.getRightChild());
				}
			}
			if (!toVisit.isEmpty()) {
				next[0] = toVisit.remove(0);
				hasNext[0] = true;
			} else {
				hasNext[0] = false;
			}
		}
		
		boolean visitData(Entry data) {
			if (data.getLowVal() >= lBound && data.getHiVal() <= rBound) {
				results.add(data);
				return false;
			} else if (data.getLowVal() > rBound || data.getHiVal() < lBound) {
				results.add(data);
				return false;
			}
			return true;
		}
		
	}

	
	/**
	 * Buuld the index with a bottom-up manner.
	 * @param nodes
	 * @param size
	 */
	public void buildIndex(BinaryTree[] nodes, int size) {
		// TODO Auto-generated method stub
		while (size > 1) {
			int newSize = 0;
			for (int i = 0; i < size; i += 2) {
				if (i + 1 >= size) {
					nodes[newSize ++] = nodes[i];
				} else {
					BinaryTree<Integer, Entry> leftNode = nodes[i];
					BinaryTree<Integer, Entry> rightNode = nodes[i + 1];
					nodes[newSize ++] = new BinaryTree(null,
							new Entry(leftNode.getValue(), rightNode.getValue()),
							leftNode,
							rightNode,
							getClassValue()
							);
				}
			}
			size = newSize;
		}
		this.value = nodes[0].getValue();
		this.setLeftChild(nodes[0].getLeftChild());
		this.setRightChild(nodes[0].getRightChild());
	}
}
