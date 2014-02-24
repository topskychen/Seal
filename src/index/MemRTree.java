/**
 * 
 */
package index;


import io.IO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import party.DataOwner;
import rtree.Node;
import rtree.RTree;
import rtree.Records;
import spatialindex.IEntry;
import spatialindex.IQueryStrategy;
import spatialindex.IShape;
import spatialindex.Region;
import storagemanager.IBuffer;
import storagemanager.IStorageManager;
import storagemanager.MemoryStorageManager;
import storagemanager.PropertySet;
import storagemanager.RandomEvictionsBuffer;
import timer.Timer;
import utility.Constants;
import utility.Seal;
import utility.StatisticsUpdate;
import utility.Tuple;
import utility.VOCell;
import utility.Constants.MODE;

/**
 * @author chenqian
 *
 */
public class MemRTree extends RTree implements SearchIndex {

	HashMap<Integer, Entry> innerEntries 	= null;
	HashMap<Integer, Entry> leafEntries 	= null;
	HashSet<Integer> 		rebuildIds		= null;
	Timer					timer 			= null;
	
	public MemRTree(PropertySet ps, IStorageManager sm) {
		super(ps, sm);
		innerEntries 	= new HashMap<Integer, Entry>();
		leafEntries 	= new HashMap<Integer, Entry>();
		timer			= new Timer();
		// TODO Auto-generated constructor stub
	}
	
	public static MemRTree createTree() {
		IStorageManager sm = new MemoryStorageManager();
		IBuffer buffer = new RandomEvictionsBuffer(sm, 10, false); // no buffer due to no page reuse
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
	public ArrayList<VOCell> rangeQuery(IShape query) {
		RangeQueryStrategy rangeQueryStrategy = new RangeQueryStrategy(query);
		queryStrategy(getRootId(), rangeQueryStrategy);
		return rangeQueryStrategy.getVOCells(); 
	}
	
	/**
	 * Insert a point, if exists then replace
	 * @param entry
	 */
	public void replace(ArrayList<DataOwner> owners, Entry entry) {
		
		if (leafEntries.containsKey(entry.getId())) {
			Entry oldEntry = leafEntries.get(entry.getId());
			if (!deleteData(oldEntry.getShape(), oldEntry.getId())) {
				System.out.println("fail delete");
			}
		} 
		insertData(null, entry.getShape(), entry.getId());
		if (leafEntries.containsKey(entry.getId())) {
			Entry recEntry = leafEntries.get(entry.getId());
			recEntry.setShape(entry.getShape());
			recEntry.setTS(entry.getTS());
		}
		else {
			leafEntries.put(entry.getId(), entry);
		}
	}	
	
	@Override
	public void buildIndex(ArrayList<DataOwner> owners, ArrayList<Entry> entries, StatisticsUpdate statU) {
		if (Constants.G_MODE != MODE.REBUILD) {
			if (Constants.G_MODE == MODE.UPDATE) {
				rebuildIds = new HashSet<Integer>();
				Records records = getRecords(); records.clear();
				for (Entry entry : entries) {
					replace(owners, entry);
				}
				ArrayList<Integer> reCalcIds = records.getDataIds(this);
				timer.reset();
				for (Integer reCalcId : reCalcIds) {
					Entry entry = leafEntries.get(reCalcId);
					int[] comPre = DataOwner.comPre(this, entry.getShape(), entry.getId());
					entry.setTuple(new Tuple(entry.getId(), entry.getShape(), entry.getTS(), comPre, INDEX_TYPE.RTree)); // update the leaf entry
					entry.setSeal(new Seal(entry.getTuple(), owners.get(entry.getId()).getSS(entry.getTS())));
					statU.appendBandWidth(IO.toBytes(entry).length);
					ArrayList<Integer> path = new ArrayList<Integer>(); 
					getPath(getRootId(), entry.getShape(), entry.getId(), path); 
					rebuildIds.addAll(path);
				}
				rebuildIds.add(getRootId());
				buildIndex(getRootId());
				rebuildIds.clear(); 
				rebuildIds = null;
				timer.stop();
				statU.appendBuildTime(timer.timeElapseinMs());
				statU.appendNum(entries.size(), reCalcIds.size());
			} else {
				throw new IllegalStateException("This mode is not supported");
			}
		} else {
			for (int i = 0; i < entries.size(); i ++) {
				Entry entry = entries.get(i);
				insertData(null, entry.getShape(), entry.getId());
				leafEntries.put(entry.getId(), entry);
			}
			buildIndex(getRootId());
		}
	}
	
	public void buildIndex(int id) {
		if (rebuildIds != null) {
			if (!rebuildIds.contains(id)) return;
			rebuildIds.remove(id);
		}
		Node node = readNode(id);
		Entry[] entries = new Entry[node.getChildrenCount()];
		for (int i = 0; i < node.getChildrenCount(); i ++) {
			//TODO
			int cId = node.getChildIdentifier(i);
			if (!node.isLeaf()) {
				buildIndex(cId);
				entries[i] = innerEntries.get(cId);
			} else {
				entries[i] = leafEntries.get(cId);
			}
		}
		Entry entry = new Entry(id, entries, -1);
		entry.setShape(node.getShape());
		innerEntries.put(id, entry);
		return;
	}
	
	public boolean getPath(int nodeId, IShape p, int id, ArrayList<Integer> path) {
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
//					path.add(id);
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
		
		
		
		
		public RangeQueryStrategy(IShape query) {
			this.query = query; 
		}
		
		public ArrayList<VOCell> getVOCells() {
			ArrayList<VOCell> voCells = new ArrayList<VOCell>();
			for (java.util.Map.Entry<Integer, Boolean> entry : innerEntryHM.entrySet()) {
				if (entry.getValue()) {
					RetrieveStrategy rs = new RetrieveStrategy();
					queryStrategy(entry.getKey(), rs);
					voCells.add(new VOCell(rs.getTuples(), innerEntries.get(entry.getKey())));
				} else {
					voCells.add(new VOCell(new ArrayList<Tuple>(), innerEntries.get(entry.getKey())));
				}
			}
			for (java.util.Map.Entry<Integer, Boolean> entry : leafEntryHM.entrySet()) {
				Entry e = leafEntries.get(entry.getKey());
				ArrayList<Tuple> tuples = new ArrayList<Tuple>();
				if (entry.getValue()) {
					tuples.add(e.getTuple());
				}
				voCells.add(new VOCell(tuples, e));
			}
			return voCells;
		}

		@Override
		public void getNextEntry(IEntry e, int[] nextEntry, boolean[] hasNext) {
			
			Node node = (Node) e;
			
			for (int i = 0; i < node.getChildrenCount(); i ++) {
				Region shape = (Region) node.getChildShape(i);
				Integer cId = node.getChildIdentifier(i);
				if (query.contains(shape)) {
					if (node.isLeaf()) {
						leafEntryHM.put(cId, true);
					} else {
						innerEntryHM.put(cId, true);
					}
				} else if (!query.intersects(shape)){
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
