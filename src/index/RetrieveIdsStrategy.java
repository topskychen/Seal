/**
 * 
 */
package index;

import java.util.ArrayList;

import memoryindex.BinaryTree;
import memoryindex.IQueryStrategy;

/**
 * @author chenqian
 *
 */
public class RetrieveIdsStrategy implements IQueryStrategy {

	private ArrayList<BinaryTree> toVisit = new ArrayList<BinaryTree>();
	ArrayList<Integer> ids = null;
	
	/**
	 * 
	 */
	public RetrieveIdsStrategy() {
		// TODO Auto-generated constructor stub
		ids = new ArrayList<>();
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
			Entry data = (Entry) n.getLeftChild().getValue();
			ids.add(data.getId());
		}
		if (!toVisit.isEmpty()) {
			next[0] = toVisit.remove(0);
			hasNext[0] = true;
		} else {
			hasNext[0] = false;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
