/**
 * 
 */
package index;

import index.BinarySearchTree.RangeQueryStrategy;

import java.util.ArrayList;
import java.util.HashMap;

import rtree.Node;
import rtree.RTree;
import spatialindex.IEntry;
import spatialindex.IQueryStrategy;
import spatialindex.IShape;
import spatialindex.Point;
import spatialindex.Region;
import storagemanager.IBuffer;
import storagemanager.IStorageManager;
import storagemanager.MemoryStorageManager;
import storagemanager.PropertySet;
import storagemanager.RandomEvictionsBuffer;
import utility.Constants;
import utility.Query;
import utility.Tuple;
import utility.VOCell;

/**
 * @author chenqian
 *
 */
public class MemRTree extends RTree implements SearchIndex {

	HashMap<Integer, Entry> innerEntries 	= null;
	HashMap<Integer, Entry> leafEntries 	= null;
	
	public MemRTree(PropertySet ps, IStorageManager sm) {
		super(ps, sm);
		innerEntries = new HashMap<Integer, Entry>();
		leafEntries = new HashMap<Integer, Entry>();
		// TODO Auto-generated constructor stub
	}
	
	public static MemRTree createTree() {
		IStorageManager sm = new MemoryStorageManager();
		IBuffer buffer = new RandomEvictionsBuffer(sm, 10, false);
		PropertySet ps = new PropertySet();
		ps.setProperty("FillFactor", new Double(0.7));
		ps.setProperty("IndexCapacity", new Integer(Constants.F));
		ps.setProperty("LeafCapacity", new Integer(Constants.F));
		ps.setProperty("Dimension", new Integer(2));
		return new MemRTree(ps, buffer);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<VOCell> rangeQuery(Query query) {
		RangeQueryStrategy rangeQueryStrategy = new RangeQueryStrategy(query);
		queryStrategy(getRootId(), rangeQueryStrategy);
		return rangeQueryStrategy.getVOCells(); 
	}

	@Override
	public void buildIndex(ArrayList<Entry> entries) {
		// TODO Auto-generated method stub
		for (int i = 0; i < entries.size(); i ++) {
			Entry entry = entries.get(i);
			insertData(null, new spatialindex.Point(entry.getLB().doubleCoords()), entry.getTuple().getId());
			leafEntries.put(entry.getTuple().getId(), entry);
		}
		buildIndex(getRootId());
	}
	
	public void buildIndex(int id) {
		Node node = readNode(id);
		Entry[] entries = new Entry[node.getChildrenCount()];
		for (int i = 0; i < node.getChildrenCount(); i ++) {
			//TODO
			int cId = node.getChildIdentifier(i);
			if (node.getLevel() != 0) {
				buildIndex(cId);
				entries[i] = innerEntries.get(cId);
			} else {
				entries[i] = leafEntries.get(cId);
			}
		}
		innerEntries.put(id, new Entry(id, entries));
		return;
	}
	
	public boolean getPath(int nodeId, Point p, int id, ArrayList<Integer> path) {
		Node node = readNode(nodeId);
		for (int i = 0; i < node.getChildrenCount(); i ++) {
			if (node.getLevel() != 0) {
				if (node.getChildShape(i).contains(p)) {
					if (getPath(node.getChildIdentifier(i), p, id, path)) {
						path.add(node.getChildIdentifier(i));
						return true;
					}
				}
			} else {
				if (node.getChildIdentifier(i) == id) {
					path.add(id);
					return true;
				}
			}
		}
		return false;
	}
	
	class RangeQueryStrategy implements IQueryStrategy {

		private ArrayList<Integer> 			toVisit 		= new ArrayList<Integer>();
		private HashMap<Integer, Boolean> 	innerEntryHM 	= new HashMap<Integer, Boolean>();
		private HashMap<Integer, Boolean> 	leafEntryHM 	= new HashMap<Integer, Boolean>();
		private IShape 						query			= null;
		
		
		
		
		public RangeQueryStrategy(Query query) {
			this.query = new Region(query.getLB().doubleCoords(), query.getHB().doubleCoords()); 
		}
		
		public ArrayList<VOCell> getVOCells() {
			ArrayList<VOCell> voCells = new ArrayList<VOCell>();
			for (java.util.Map.Entry<Integer, Boolean> entry : innerEntryHM.entrySet()) {
				if (entry.getValue()) {
					RetrieveStrategy rs = new RetrieveStrategy();
					queryStrategy(entry.getKey(), rs);
					voCells.add(new VOCell(rs.getTuples(), innerEntries.get(entry.getKey())));
				} else {
					voCells.add(new VOCell(null, innerEntries.get(entry.getKey())));
				}
			}
			for (java.util.Map.Entry<Integer, Boolean> entry : leafEntryHM.entrySet()) {
				Entry e = leafEntries.get(entry.getKey());
				if (entry.getValue()) {
					ArrayList<Tuple> tuples = new ArrayList<Tuple>();
					tuples.add(e.getTuple());
					voCells.add(new VOCell(tuples, e));
				} else {
					voCells.add(new VOCell(null, e));
				}
			}
			return voCells;
		}

		@Override
		public void getNextEntry(IEntry e, int[] nextEntry, boolean[] hasNext) {
			
			Node node = (Node) e;
			
			for (int i = 0; i < node.getChildrenCount(); i ++) {
				Region region = (Region) node.getChildShape(i);
				Integer cId = node.getChildIdentifier(i);
				if (query.contains(region)) {
					if (node.isLeaf()) {
						leafEntryHM.put(cId, true);
					} else {
						innerEntryHM.put(cId, true);
					}
				} else if (!query.intersects(region)){
					if (node.isLeaf()) {						
						leafEntryHM.put(cId, false);
					} else {
						innerEntryHM.put(cId, false);
					}
				} else {
					toVisit.add(cId);
				}
			}
			
			if (!toVisit.isEmpty()) {
				nextEntry[0] = toVisit.remove(0);
				hasNext[0] = true;
			} else {
				hasNext[0] = false;
			}
		}
	}
	
	class RetrieveStrategy implements IQueryStrategy {

		private ArrayList<Integer> 	toVisit = new ArrayList<Integer>();
		ArrayList<Tuple> 			tuples	= null;
		
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
		public void getNextEntry(IEntry e, int [] next, boolean[] hasNext) {
			// TODO Auto-generated method stub
			Node node = (Node) e;
			
			for (int i = 0; i < node.getChildrenCount(); i ++) {
				int cId = node.getChildIdentifier(i);
				if (node.isLeaf()) {
					tuples.add(leafEntries.get(cId).getTuple());
				} else {
					toVisit.add(cId);
				}
			}
			if (!toVisit.isEmpty()) {
				next[0] = toVisit.remove(0);
				hasNext[0] = true;
			} else {
				hasNext[0] = false;
			}
		}

	}

	@Override
	public INDEX_TYPE getType() {
		// TODO Auto-generated method stub
		return INDEX_TYPE.RTree;
	}
}
