/**
 * 
 */
package index;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import party.DataOwner;
import spatialindex.IShape;
import spatialindex.Point;
import spatialindex.Region;
import utility.StatisticsUpdate;
import utility.Tuple;
import utility.VOCell;
import memoryindex.BinaryTree;
import memoryindex.IQueryStrategyBT;

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
	public ArrayList<VOCell> rangeQuery(IShape query) {
		// TODO Auto-generated method stub
		RangeQueryStrategy rangeQueryStrategy = new RangeQueryStrategy(query);
		queryStrategy(this, rangeQueryStrategy);
		return rangeQueryStrategy.getVOCells();
	}

	
	@Override
	public void buildIndex(ArrayList<DataOwner> owners, ArrayList<Entry> entries, StatisticsUpdate statU) {
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

		
	class RangeQueryStrategy implements IQueryStrategyBT {

		private ArrayList<BinaryTree> 	toVisit 	= new ArrayList<BinaryTree>();
		private ArrayList<BinaryTree> 	inRange 	= new ArrayList<BinaryTree>();
		private ArrayList<BinaryTree> 	outRange 	= new ArrayList<BinaryTree>();
		private IShape 					query 		= null;
		
		
		public RangeQueryStrategy(IShape query) {
			super();
			this.query = query;
		}

		public ArrayList<VOCell> getVOCells() {
			ArrayList<VOCell> voCells = new ArrayList<VOCell>();
			for (BinaryTree tree : inRange) {
				RetrieveStrategy qs = new RetrieveStrategy();
				queryStrategy(tree, qs);
				voCells.add(new VOCell(qs.getTuples(), (Entry)tree.getValue()));
			}
			for (BinaryTree tree: outRange) {
				voCells.add(new VOCell(new ArrayList<Tuple>(), (Entry) tree.getValue()));
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
			IShape p = data.getShape();
			if (query.contains(p)) {
				inRange.add(tree);
				return false;
			} else if (!query.intersects(p)) {
				outRange.add(tree);
				return false;
			}
			return true;
		}
		
	}
	
	class RetrieveStrategy implements IQueryStrategyBT {

		private ArrayList<BinaryTree> 	toVisit = new ArrayList<BinaryTree>();
		ArrayList<Tuple> 				tuples	= null;
		
		public ArrayList<Tuple> getTuples() {
			return tuples;
		}
		
		/**
		 * 
		 */
		public RetrieveStrategy() {
			// TODO Auto-generated constructor stub
			tuples = new ArrayList<Tuple>();
		}

		/* (non-Javadoc)
		 * @see memoryindex.IQueryStrategy#getNextEntry(memoryindex.BinaryTree, memoryindex.BinaryTree[], boolean[])
		 */
		@Override
		public void getNextEntry(BinaryTree n, BinaryTree[] next, boolean[] hasNext) {
			// TODO Auto-generated method stub
			if (!n.isLeftChildEmpty()) {
				toVisit.add(n.getLeftChild());
			}
			if (!n.isRightChildEmpty()) {
				toVisit.add(n.getRightChild());
			}
			if (n.isLeaf()) {
				Entry data = (Entry) n.getValue();
				tuples.add(data.getTuple());
			}
			if (!toVisit.isEmpty()) {
				next[0] = toVisit.remove(0);
				hasNext[0] = true;
			} else {
				hasNext[0] = false;
			}
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
							new Entry((Entry)leftNode.getValue(), (Entry)rightNode.getValue(), -1),
							leftNode,
							rightNode,
							getClassValue()
							);
//					System.out.println(nodes[newSize - 1].getValue());
//					System.out.println("[" + cnt(leftNode) + "+" + cnt(rightNode) + "] = " + cnt(nodes[newSize - 1]));
//					if (cnt(leftNode).add(cnt(rightNode)).equals(cnt(nodes[newSize - 1])) == false) {
//						System.out.println("What the fuck!");
//					}
//					System.out.println("cnt o: " + ((Entry) nodes[newSize - 1].getValue()).getNO());
//					System.out.println("cnt r: " + ((Entry) nodes[newSize - 1].getValue()).getSeal().getCnt(null));
				}
			}
			size = newSize;
		}
		this.value = nodes[0].getValue();
		this.setLeftChild(nodes[0].getLeftChild());
		this.setRightChild(nodes[0].getRightChild());
	}
	
	public int no(BinaryTree node) {
		return ((Entry) node.getValue()).getNO();
	}
	public BigInteger cnt(BinaryTree node) {
		return ((Entry) node.getValue()).getSeal().getCnt(null);
	}

	@Override
	public INDEX_TYPE getType() {
		// TODO Auto-generated method stub
		return INDEX_TYPE.BTree;
	}
}
