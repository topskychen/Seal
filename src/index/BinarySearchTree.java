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
import utility.VO;
import utility.VOCell;
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
	public ArrayList<VOCell> rangeQuery(Query query) {
		// TODO Auto-generated method stub
		RangeQueryStrategy rangeQueryStrategy = new RangeQueryStrategy(query.getLB().getCoord(0), query.getHB().getCoord(0));
		queryStrategy(rangeQueryStrategy);
		return rangeQueryStrategy.getVOCells();
	}

	/* (non-Javadoc)
	 * @see index.SearchIndex#buildIndex(index.Trajectory, crypto.PMAC)
	 */
	@Override
	public void buildIndex(ArrayList<Entry> entries) {
		// TODO Auto-generated method stub
		
		BinaryTree[] nodes = new BinaryTree[entries.size()]; int size = 0;
		for (int i = 0; i < entries.size(); i ++) {
			nodes[size ++] = new BinaryTree<Integer, Entry>(i, entries.get(i), getClassValue());
		}
		buildIndex(nodes, size);
//		flush();
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
		private ArrayList<BinaryTree> trees = new ArrayList<BinaryTree>();
		private int lBound, rBound;
		
		
		public RangeQueryStrategy(int lBound, int rBound) {
			super();
			this.lBound = lBound;
			this.rBound = rBound;
		}

		public ArrayList<VOCell> getVOCells() {
			ArrayList<VOCell> voCells = new ArrayList<>();
			for (BinaryTree tree : trees) {
				RetrieveStrategy qs = new RetrieveStrategy();
				queryStrategy(tree, qs);
				voCells.add(new VOCell(qs.getIds(), qs.getTuples(), (Entry)tree.getValue()));
			}
			return voCells;
		}

		@Override
		public void getNextEntry(BinaryTree n, BinaryTree[] next,
				boolean[] hasNext) {
			// TODO Auto-generated method stub
			if (!n.isLeftChildEmpty()) {
				if (visitData(n.getLeftChild())) {
					toVisit.add(n.getLeftChild());
				}
			}
			if (!n.isRightChildEmpty()) {
				if (visitData(n.getRightChild())) {
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
		
		boolean visitData(BinaryTree tree) {
			Entry data = (Entry) tree.getValue(); 
			if (data.getLowVal() >= lBound && data.getHiVal() <= rBound) {
				trees.add(tree);
				return false;
			} else if (data.getLowVal() > rBound || data.getHiVal() < lBound) {
				trees.add(tree);
				return false;
			}
			return true;
		}
		
	}
	
	public void queryStrategy(BinaryTree tree, final IQueryStrategy qs) {
		BinaryTree[] next = new BinaryTree[]{tree};
		while (true) {
			BinaryTree n = next[0];
			boolean[] hasNext = new boolean[] {false};
			qs.getNextEntry(n, next, hasNext);
			if (hasNext[0] == false) break;
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
