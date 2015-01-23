/**
 * 
 */
package index;

import io.IO;
import io.RW;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import rtree.Node;
import rtree.RTree;
import spatialindex.IData;
import spatialindex.IEntry;
import spatialindex.INode;
import spatialindex.IQueryStrategy;
import spatialindex.IShape;
import spatialindex.IVisitor;
import spatialindex.Region;
import storagemanager.IBuffer;
import storagemanager.IStorageManager;
import storagemanager.MemoryStorageManager;
import storagemanager.PropertySet;
import storagemanager.RandomEvictionsBuffer;
import utility.Global;
import utility.Simulator;
import utility.StatisticsUpdate;
import utility.Tuple;

/**
 * @author chenqian
 * wrtie function is commented right now.
 */
public class MemRTree extends RTree implements SearchIndex, RW {

	Map<Integer, Entry>		innerEntries	= null;
	Map<Integer, Entry>		leafEntries		= null;
	HashSet<Integer>		rebuildIds		= null;
	Simulator				simulator 		= null;

	public MemRTree(PropertySet ps, IStorageManager sm, Simulator simulator) {
		super(ps, sm);
		innerEntries = new HashMap<Integer, Entry>();
		leafEntries = new HashMap<Integer, Entry>();
		this.simulator = simulator;
	}

	public static MemRTree createTree(Simulator simulator) {
		IStorageManager sm = new MemoryStorageManager();
		IBuffer buffer = new RandomEvictionsBuffer(sm, 10, false); // no
																		// buffer
																		// due
																		// to no
																		// page
																		// reuse
		PropertySet ps = new PropertySet();
		ps.setProperty("FillFactor", new Double(0.7));
		ps.setProperty("IndexCapacity", new Integer(Global.FANOUT));
		ps.setProperty("LeafCapacity", new Integer(Global.FANOUT));
		ps.setProperty("Dimension", new Integer(simulator.getDim()));
		return new MemRTree(ps, buffer, simulator);
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
	 * 
	 * @param entry
	 */
	public void replace(Entry entry) {
		if (leafEntries.containsKey(entry.getId())) {
			Entry oldEntry = leafEntries.get(entry.getId());
			if (!deleteData(oldEntry.getShape(), oldEntry.getId())) {
				System.out.println("fail delete");
			}
		}
		insertData(null, entry.getShape(),  entry.getId());
		leafEntries.put(entry.getId(), entry);
	}
	
//	public void solve() {
//		if (simulator.getMode() == MODE.UPDATE || simulator.getMode() == MODE.LOOSE) {
//			rebuildIds = new HashSet<Integer>();
//			HashSet<Integer> reCalcIds = new HashSet<Integer>();
//			Records records = getRecords();
//			records.clear();
//			int outOfBound = 0;
//			for (Entry entry : entries) {
//				if (simulator.getMode() == MODE.LOOSE) {
//					if (leafEntries.get(entry.getId()) != null) {
//						Point p = (Point) entry.getShape();
//						Region region = new Region(new double[] {
//								p.getCoord(0) - simulator.getRtreeRegionL(),
//								p.getCoord(1) - simulator.getRtreeRegionL() },
//								new double[] {
//										p.getCoord(0) + simulator.getRtreeRegionL(),
//										p.getCoord(1) + simulator.getRtreeRegionL() });
//						entry.setShape(region);
//					} else {
//						Entry oldEntry = leafEntries.get(entry.getId());
//						if (!oldEntry.getShape().contains(entry.getShape())) { // out
//							Point p = (Point) entry.getShape();
//							Region region = new Region(
//									new double[] {
//											p.getCoord(0) - simulator.getRtreeRegionL(),
//											p.getCoord(1) - simulator.getRtreeRegionL() },
//									new double[] {
//											p.getCoord(0) + simulator.getRtreeRegionL(),
//											p.getCoord(1) + simulator.getRtreeRegionL() });
//							entry.setShape(region);
//							outOfBound++;
//						} else {
//							entry.setShape(oldEntry.getShape());
//						}
//					}
//				}
//				replace(entry);
//				reCalcIds.add( entry.getId());
//			}
//			Global.TIMER.reset();
////			for (int i = 0; i < outOfBound; i++) {
////				Seal.getSample(i);
////			}
//			Global.TIMER.stop();
//			if (entries.size() != simulator.getTotalN()) {
//				statU.appendDOCPU(Global.TIMER.timeElapseinMs());
//			}
//			for (Integer id : records.getVisitedIds()) {
//				rebuildIds.add(id);
//			}
//			// ArrayList<Integer> reCalcIds = records.getDataIds(this);
//			for (Entry entry : leafEntries.values()) {
//				if (entry == null) continue;
//				int[] comPre = DataOwner.comPre(this, entry.getShape(),
//						 entry.getId());
//				if (!Arrays.equals(comPre, entry.getComPre())
//						|| entry.getComPre() == null) {
//					reCalcIds.add( entry.getId());
//				}
//			}
//			Global.TIMER.reset();
//			int bandWidth = 0;
//			for (Integer reCalcId : reCalcIds) {
//				Entry entry = leafEntries.get(reCalcId);
//				int[] comPre = DataOwner.comPre(this, entry.getShape(),
//						 entry.getId());
//				entry.setComPre(comPre);
//				// entry.setTuple(new Tuple(entry.getId(), entry.getShape(),
//				// entry.getTS(), comPre, INDEX_TYPE.RTree));
//				entry.setSeal(new Seal(entry.getTuple()	, owners.get(
//						 entry.getId()).getSS(entry.getTS())));
//				bandWidth += IO.toBytes(entry).length;
//				ArrayList<Integer> path = new ArrayList<Integer>();
//				getPath(getRootId(), entry.getShape(),  entry.getId(), path);
//				rebuildIds.addAll(path);
//			}
//			rebuildIds.add(getRootId());
//			buildIndex(getRootId());
//			rebuildIds.clear();
//			rebuildIds = null;
//			timer.stop();
//			if (entries.size() != Global.TOTN) {
//				statU.append(timer.timeElapseinMs(), bandWidth);
//				statU.appendNum(entries.size(), reCalcIds.size());
//			}
//	}
	
	@Override
	public void updateIndex(ArrayList<Entry> entries, StatisticsUpdate statU) {
//			timer.reset();
		for (int i = 0; i < entries.size(); i++) {
			Entry entry = entries.get(i);
//				timer.resume();
			insertData(null, entry.getShape(),  entry.getId());
//				timer.pause();
			leafEntries.put(entry.getId(), entry);
		}
		buildIndex(getRootId());
//			timer.stop();
//			System.out.println("R building index : " + timer.timeElapseinMs() + "ms");
	}

	public void buildIndex(int id) {
		if (rebuildIds != null) {
			if (!rebuildIds.contains(id))
				return;
			rebuildIds.remove(id);
		}
		Node node = readNode(id);
		Entry[] entries = new Entry[node.getChildrenCount()];
		for (int i = 0; i < node.getChildrenCount(); i++) {
			// TODO
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

	@Override
	public int[] getPrefix(IShape p) {
		int[] path = new int[Global.L];
		getPath(getRootId(), p, path, 0);
		return path;
	}
	
	public boolean getPath(int nodeId, IShape p, int[] path, int lev) {
		if (lev == Global.L) return true;
		Node node = readNode(nodeId);
		path[lev] = nodeId;
		for (int i = 0; i < node.getChildrenCount(); i++) {
			if (node.getLevel() != 0) {
				if (node.getChildShape(i).contains(p)) {
					if (getPath(node.getChildIdentifier(i), p, path, lev + 1)) return true;
				}
			} else {
				if (node.getChildShape(i).touches(p)) {
					if (lev + 1 < Global.L) path[lev + 1] = node.getChildIdentifier(i);
					return true;
				}
			}
		}
		return false;
	}

	class RangeQueryStrategy implements IQueryStrategy {

		private ArrayList<Integer>	toVisit	= new ArrayList<Integer>();
		private ArrayList<VOCell>	voCells	= new ArrayList<VOCell>();
		private ArrayList<VOCell>	resVOCells	= null;
		private IShape				query	= null;

		public RangeQueryStrategy(IShape query) {
			this.query = query;
		}

		public ArrayList<VOCell> getVOCells() {
			if (resVOCells == null) {
				Map<Integer, List<VOCell>> map = new HashMap<Integer, List<VOCell>>();
				for (VOCell cell : voCells) {
					int lev = cell.getLev();
					if (!map.containsKey(lev)) map.put(lev, new ArrayList<VOCell>());
					map.get(lev).add(cell);
				}
				resVOCells = new ArrayList<VOCell>();
				for (int lev = 0; lev < Global.L; ++lev) {
					if (map.containsKey(lev)) {
						List<VOCell> cells = map.get(lev);
						if (cells.size() != 0) {
							VOCell res = cells.get(0);
							for (int i = 1; i < cells.size(); ++i) {
								res.fold(cells.get(i));
							}
							resVOCells.add(res);
						}
					}
				}
			}
			return resVOCells;
		}

		@Override
		public void getNextEntry(IEntry e, int[] nextEntry, boolean[] hasNext) {

			Node node = (Node) e;

			for (int i = 0; i < node.getChildrenCount(); i++) {
				Region shape = (Region) node.getChildShape(i);
				Integer cId = node.getChildIdentifier(i);
				if (query.contains(shape)) {
					if (node.isLeaf()) {
						Entry entry = leafEntries.get(cId);
						voCells.add(new VOCell(entry.getId(), entry.getTuple(), entry));
					} else {
						Entry entry = innerEntries.get(cId);
						RetrieveStrategy rs = new RetrieveStrategy();
						queryStrategy(cId, rs);
						voCells.add(new VOCell(rs.getAnsIds(), rs.getTuples(), entry));
					}
				} else if (!query.intersects(shape)) {
					if (node.isLeaf()) {
						voCells.add(new VOCell(-1, null,
								leafEntries.get(cId)));
					} else {
						voCells.add(new VOCell(null, null,
								innerEntries.get(cId)));
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

		private ArrayList<Integer>	toVisit	= new ArrayList<Integer>();
		ArrayList<Tuple>			tuples	= null;

		public Set<Integer> getAnsIds() {
			Set<Integer> ans = new HashSet<Integer>();
			for (Tuple tuple : tuples) {
				ans.add(tuple.getId());
			}
			return ans;
		}
		
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see memoryindex.IQueryStrategy#getNextEntry(memoryindex.BinaryTree,
		 * memoryindex.BinaryTree[], boolean[])
		 */
		@Override
		public void getNextEntry(IEntry e, int[] next, boolean[] hasNext) {
			// TODO Auto-generated method stub
			Node node = (Node) e;

			for (int i = 0; i < node.getChildrenCount(); i++) {
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

	@Override
	public void read(DataInputStream ds) {
		innerEntries = new HashMap<Integer, Entry>();
		leafEntries = new HashMap<Integer, Entry>();
		int num = IO.readInt(ds);
		for (int i = 0; i < num; i++) {
			int id = IO.readInt(ds);
			Entry entry = new Entry();
			entry.read(ds);
			innerEntries.put(id, entry);
		}
		num = IO.readInt(ds);
		for (int i = 0; i < num; i++) {
			int id = IO.readInt(ds);
			Entry entry = new Entry();
			entry.read(ds);
			leafEntries.put(id, entry);
		}
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
//		IO.writeInt(ds, innerEntries.size());
//		for (java.util.Map.Entry<Integer, Entry> entry : innerEntries.entrySet()) {
//			IO.writeInt(ds, entry.getKey());
//			entry.getValue().write(ds);
//		}
//		IO.writeInt(ds, leafEntries.size());
//		for (java.util.Map.Entry<Integer, Entry> entry : leafEntries.entrySet()) {
//			IO.writeInt(ds, entry.getKey());
//			entry.getValue().write(ds);
//		}
	}

	@Override
	public ArrayList<IShape> kNN(IShape query, int k) {
		// TODO Auto-generated method stub
		KNNVisitor visitor = new KNNVisitor();
		nearestNeighborQuery(k, query, visitor);
		return visitor.getPoints();
	}
	
	class KNNVisitor implements IVisitor {

		ArrayList<IShape> points = new ArrayList<IShape>();
		
		@Override
		public void visitNode(INode n) {
			// TODO Auto-generated method stub
		}

		@Override
		public void visitData(IData d) {
			points.add(d.getShape());
		}
		
		public ArrayList<IShape> getPoints() {
			return points;
		}
	}
	
	boolean dominate(IShape a, IShape b) {
		double[] l1 = a.getMBR().m_pLow;
		double[] l2 = b.getMBR().m_pLow;
		for (int i = 0; i < l1.length; ++i) {
			if (l2[i] < l1[i]) return false;
		}
		return true;
	}
	
	boolean dominate(ArrayList<IShape> points, IShape candidate) {
		for (IShape point : points) {
			if (dominate(point, candidate)) return true;
		}
		return false;
	}

	@Override
	public ArrayList<IShape> skyline() {
		PriorityQueue<SLEntry> queue = new PriorityQueue<SLEntry>();
		queue.add(new SLEntry(getRoot()));
		ArrayList<IShape> res = new ArrayList<IShape>();
		
		while (!queue.isEmpty()) {
			SLEntry first = queue.poll();
			if (first.entry instanceof Node) {
				Node n = (Node) first.entry;
				for (int c = 0; c < n.getChildrenCount(); ++c) {
					IEntry e = null;
					if (n.getLevel() == 0) { // leaf node
						if (!dominate(res, n.getChildShape(c))) e = new LeafEntry(n.getChildShape(c), n.getChildIdentifier(c));
					} else {
						if (!dominate(res, n.getChildShape(c))) e = readNode(n.getChildIdentifier(c));
					}
					if (e != null) queue.add(new SLEntry(e));
				}
			} else {
				if (!dominate(res, first.entry.getShape())) {
					res.add(first.entry.getShape());
				}
			}
		}
		return res;
	}
	
	class SLEntry implements Comparable<SLEntry> {

		IEntry entry = null;
		double minDist = 0;
		
		public SLEntry(IEntry _entry) {
			entry = _entry;
			double[] low = entry.getShape().getMBR().m_pLow;
			for (int i = 0; i < low.length; ++i) {
				minDist += low[i];
			}
		}
		
		@Override
		public int compareTo(SLEntry o) {
			// TODO Auto-generated method stub
			if (minDist < o.minDist) return -1;
			else if (minDist > o.minDist) return 1;
			return 0;
		}
	}
	
	class LeafEntry implements IEntry {
		int id;
		IShape shape;

		LeafEntry(IShape _shape, int _id) { id = _id; shape = _shape; }
		public int getIdentifier() { return id; }
		public IShape getShape() { return shape; }
	}

	@Override
	public byte[] toBytes() {
		return IO.toBytes(this);
	}
}
