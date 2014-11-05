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

import memoryindex.IQueryStrategyQT;
import memoryindex.IVisitorQT;
import memoryindex.QuadEntry;
import memoryindex.QuadTree;
import spatialindex.IShape;
import spatialindex.Point;
import spatialindex.Region;
import utility.Global;
import utility.Global.MODE;
import utility.Global.OP;
import utility.Simulator;
import utility.StatisticsUpdate;
import utility.Tuple;

/**
 * 
 * For lazy update. L is short for lastest updated. U is short for un-updated
 * ones.
 * 
 * @author chenqian
 * 
 */
public class MemQTree extends QuadTree implements SearchIndex, RW {

	private int								lev				= 0;
	public HashMap<Integer, Entry>			L				= null;
	public HashMap<Integer, UpdateEntry>	U				= null;
	Simulator simulator 									= null;
	
	
	public static MemQTree createTree(Simulator sim) {
		double[] low = new double[sim.getDim()];
		double[] high = new double[sim.getDim()];
		for (int i = 0; i < sim.getDim(); ++i) {
			low[i] = -1;
			high[i] = Global.BOUND + 1;
		}
		
		return new MemQTree(sim.getDim(), sim.getCapacity(), new Region(low, high), 0, 0, sim);
	}
	
	/**
	 * It is used for checking the counting is right or not.
	 * @return
	 */
	public boolean checkTree() {
 		int tmp = 0;
 		if (!isLeaf()) {
			for (QuadEntry entry : getEntries()) {
				if (entry != null) tmp += ((Entry) entry).getNO();
			}
 		} else {
 			for (Entry entry : L.values()) {
 				tmp += entry.getNO();
 			}
 		}
		if (tmp != getCnt()) {
			System.err.println(this);
			return false;
		}
		if (!isLeaf()) {
			if (getChTrees() != null) {
				for (int i = 0; i < getDivision(); ++i) {
					if (!((MemQTree) getChTree(i)).checkTree()) return false;
				}
			}
		}
		return true;
	}
	
	
	/**
	 * @param capacity
	 * @param boundary
	 */
	public MemQTree(int dim, int capacity, Region boundary, int lev, long id, Simulator simulator) {
		super(dim, capacity, boundary);
		this.simulator = simulator;
		this.lev = lev;
		L = new HashMap<Integer, Entry>();
		U = new HashMap<Integer, UpdateEntry>();
		// if (Constants.G_MODE != MODE.REBUILD && !isLeaf()) {
		if (!isLeaf()) {
			ArrayList<QuadEntry> entries = new ArrayList<QuadEntry>();
			for (int i = 0; i < getDivision(); i++) {
				entries.add(null);
			}
			setEntries(entries);
		}
		setId(id);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	@Override
	public ArrayList<VOCell> rangeQuery(IShape query) {
		RangeQueryStrategy rangeQueryStrategy = new RangeQueryStrategy(query);
		queryStrategy(this, rangeQueryStrategy);
		return rangeQueryStrategy.getVOCells();
	}

	@Override
	public void updateIndex(ArrayList<Entry> entries, StatisticsUpdate statU) {
		Global.TIMER.reset();
		for (Entry entry : entries) {
			if (simulator.getMode() == MODE.LAZY) {
				putU(entry.getId(), new UpdateEntry(OP.ADD, entry));
			} else {
				replace(entry);
			}
		}

		if (entries.size() != simulator.getTotalN()) { // means the first time to build
			if (simulator.getMode() == MODE.LAZY)
				pushU(simulator.getBufferSize());
			Global.TIMER.stop();
			statU.append(Global.TIMER.timeElapseinMs(), 0);
		} else {
			pushU(0);
		}
		
		
		if (simulator.getMode() == MODE.REBUILD) { // push L to entries
			pushLtoEntries();
		}
	}
	
	/**
	 * Push entries in L to entries in leaf node, the quadtree will need entries. See nearestneighbor search in quadtree.
	 */
	void pushLtoEntries() {
		if (isLeaf()) {
			ArrayList<QuadEntry> entries = new ArrayList<QuadEntry>();
			for (Entry entry : L.values()) {
				if (entry == null) {
					System.out.println("wanring!");
				}
				entries.add(entry);
			}
			setEntries(entries);
		} else if (getCnt() != 0) {
			for (int i = 0; i < getDivision(); ++i) {
				((MemQTree) getChTree(i)).pushLtoEntries();
			}
		}
	}

	/**
	 * Just put update entry to the map U
	 * @param id
	 * @param entry
	 */
	public void putU(int id, UpdateEntry entry) {
		if (L.containsKey(id)) {
			Entry tmp = L.get(id);
			if (tmp.getTS() > entry.getTS()) {
				return;
			}
		}
		if (U.containsKey(id) == false) {
			U.put(id, entry);
		} else {
			UpdateEntry tmp = U.get(id);
			if (tmp.getTS() <= entry.getTS()) {
				U.put(id, entry);
			}
		}
	}

	/**
	 * locate an entry to a child tree
	 * @param chTrees
	 * @param entry
	 * @return
	 */
	public int locate(MemQTree[] chTrees, Entry entry) {
		if (entry == null)
			return -1;
		for (int i = 0; i < getDivision(); i++) {
			if (chTrees[i].contains(entry)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Add an entry
	 * child tree is updated
	 * 
	 * @param chTrees
	 * @param key
	 * @param iU
	 * @param entryU
	 */
	public void addEntry(MemQTree[] chTrees, int key, int iU, UpdateEntry entryU) {
		if (simulator.getMode() == MODE.LAZY)
			chTrees[iU].putU(key, entryU); // push down
		Entry entry = (Entry) getEntry(iU);
		if (entry == null) { // if is empty
			setEntry(iU, entryU.getEntry().clone());
		} else { // otherwise update the value with entry in U
			entry.update(null, entryU.getEntry());
		}
		L.put(key, entryU.getEntry()); // update the latest updated
	}

	/**
	 * Delete an entry
	 * child tree is update
	 * 
	 * @param chTrees
	 * @param key
	 * @param iL
	 * @param entryL
	 */
	public void delEntry(MemQTree[] chTrees, int key, int iL, Entry entryL) {
		if (iL == -1)
			return;
		if (simulator.getMode() == MODE.LAZY)
			chTrees[iL].putU(key, new UpdateEntry(OP.DEL, entryL)); // push down
		Entry entry = (Entry) getEntry(iL);
		if (entry == null)
			return;
		else
			entry.update(entryL, null);
		if (entry.getNO() == 0) { // this node has no values
			setEntry(iL, null);
		}
		L.remove(key);
	}

	/**
	 * The chTrees should not be empty.
	 * 
	 * @param chTrees
	 */
	public void updateChTree(MemQTree[] chTrees, int bufferSize,
			ArrayList<Integer> toRemove) {
		int totSize = U.size();
		for (java.util.Map.Entry<Integer, UpdateEntry> entryInU : U.entrySet()) {
			if (totSize <= bufferSize)
				break;
			totSize--;
			int key = entryInU.getKey();
			toRemove.add(key);
			UpdateEntry entryU = entryInU.getValue();
			Entry entryL = L.get(key);
			int iU = locate(chTrees, entryU.getEntry());
			int iL = locate(chTrees, entryL);

			delEntry(chTrees, key, iL, entryL);
			if (entryU.getOP() == OP.ADD) {
				addEntry(chTrees, key, iU, entryU);
			} else {
				chTrees[iU].putU(key, entryU);
			}
		}
	}

	/**
	 * Update a node
	 */
	public void updateU(int bufferSize) {
		// System.out.println("U size: " + U.size());
		ArrayList<Integer> toRemove = null;
		if (bufferSize < U.size()) {
			toRemove = new ArrayList<Integer>(U.size() - bufferSize);
		}
		if (U.isEmpty())
			return;
		if (isLeaf()) { // replace the L with U
			int totSize = U.size();
			for (java.util.Map.Entry<Integer, UpdateEntry> entry : U.entrySet()) {
				if (totSize <= bufferSize)
					break;
				toRemove.add(entry.getKey());
				totSize--;
				if (entry.getValue().getOP() == OP.DEL) {
					if (L.containsKey(entry.getKey()))
						L.remove(entry.getKey());
				} else {
					L.put(entry.getKey(), entry.getValue().getEntry());
				}
			}
			setCnt(L.size());
		} else {
			updateChTree((MemQTree[]) getChTrees(), bufferSize, toRemove);
			int cnt = 0;
			for (int i = 0; i < getDivision(); i++) {
				Entry entry = (Entry) getEntry(i);
				if (entry != null) {
					cnt += entry.getNO();
				}
			}
			setCnt(cnt);
		}
		if (toRemove != null)
			for (Integer id : toRemove)
				U.remove(id);
	}

	public void pushU(int bufferSize) {
		if (!isLeaf()) {
			if (getChTrees() == null) {
				createChTrees();
			}
			if (U.size() > bufferSize) {
				updateU(bufferSize);
				MemQTree[] chTrees = (MemQTree[]) getChTrees();
				for (int i = 0; i < getDivision(); i++) {
					chTrees[i].pushU(bufferSize);
				}
			}
		} else {
			updateU(bufferSize);
		}
	}

	public void createChTrees() {
		MemQTree[] chTrees = new MemQTree[getDivision()];
		Region[] regions = subDivide(getBoundary());
		for (int i = 0; i < getDivision(); i++) {
			chTrees[i] = new MemQTree(getDim(), getCapacity(), regions[i], lev + 1,
					(((getId() + 1) << (int) (getDim())) + i - 1), simulator);
		}
		setChTrees(chTrees);
	}

	public boolean contains(Entry entry) {
		return this.getBoundary().contains(entry.getShape());
	}

	public Entry buildIndex(QuadTree tree, HashSet<Long> modified, int lev) {
		if (tree.getCnt() == 0)
			return null;
		if (!((MemQTree) tree).isLeaf()) {
			QuadTree[] chTree = tree.getChTrees();
			ArrayList<QuadEntry> entries = new ArrayList<QuadEntry>();
			ArrayList<QuadEntry> tmp = new ArrayList<QuadEntry>();
			for (int i = 0; i < getDivision(); i++) {
				Entry entry = null;
				if (modified == null
						|| modified.contains(chTree[i].getId()) == true) {
					//
					entry = buildIndex(chTree[i], modified, lev + 1);
				} else {
					entry = (Entry) tree.getEntry(i);
				}
				entries.add(entry);
				if (entry != null) {
					tmp.add(entry);
				}
			}
			tree.setEntries(entries);
			return new Entry(-1, tmp.toArray(new Entry[0]), lev);
		} else {
			Entry[] entries = new Entry[tree.getEntries().size()];
			for (int i = 0; i < entries.length; i++) {
				entries[i] = (Entry) tree.getEntry(i);
			}
			return new Entry(-1, entries, lev);
		}
	}

	@Override
	public int[] getPrefix(IShape point) {
		int[] path = new int[Global.L];
		getPath(path, (Point) point, getBoundary(), 0, 1);
		return path;
	}

	public void getPath(int[] path, Point p,
			Region boundary, int level, int id) {
		if (level == Global.L) {
			return;
		}
		path[level] = id - 1;
		Region[] regions = subDivide(boundary);
		boolean found = false;
		for (int i = 0; i < getDivision(); i++) {
			if (regions[i].contains(p)) {
				getPath(path, p, regions[i], level + 1, (id << (int)getDim()) + i);
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IllegalStateException("Point " + p + " is out of range");
		}
	}

	/**
	 * Insert a point, if exists then replace
	 * 
	 * @param entry
	 */
	public void replace(Entry entry) {
		if (L.containsKey(entry.getId())) {
			Entry oldEntry = L.get(entry.getId());
			if (delete(oldEntry) == false) {
				throw new IllegalStateException("An entry is not deleted.");
			}
		}
		if (insert(entry) == false) {
			throw new IllegalStateException("An entry is not inserted.");
		}

	}

	public boolean insert(Entry entry) {
		if (!contains(entry))
			return false;
		if (isLeaf()) {
			L.put(entry.getId(), entry);
			setCnt(L.size());
			return true;
		} else {
			if (getChTrees() == null) {
				createChTrees();
			}
			MemQTree[] chTrees = (MemQTree[]) getChTrees();
			for (int i = 0; i < getDivision(); i++) {
				if (chTrees[i].contains(entry)) {
					addEntry(chTrees,  entry.getId(), i, new UpdateEntry(null,
							entry));
					setCnt(L.size());
					if (chTrees[i].insert(entry))
						return true;
					else
						return false;
				}
			}
			// setCnt(L.size());
			System.out.println("never to reach here [add]");
			return false;
		}
	}

	public boolean delete(Entry entry) {
		if (!contains(entry))
			return false;
		if (isLeaf()) {
			L.remove(entry.getId());
			setCnt(L.size());
			return true;
		} else {
			MemQTree[] chTrees = (MemQTree[]) getChTrees();
			if (chTrees == null) {
				throw new IllegalStateException("The chTrees cannot be null.");
			}
			for (int i = 0; i < getDivision(); i++) {
				if (chTrees[i].contains(entry)) {
					delEntry(chTrees, entry.getId(), i, entry);
					setCnt(L.size());
					if (chTrees[i].delete(entry))
						return true;
					else
						return false;
				}
			}
			System.out.println("never to reach here [delete]");
			return false;
		}
	}

	class RangeQueryStrategy implements IQueryStrategyQT {

		private ArrayList<QuadTree>	toVisit	= new ArrayList<QuadTree>();
		private ArrayList<VOCell>	voCells	= new ArrayList<VOCell>();
		private ArrayList<VOCell>	resVOCells	= null;
		private IShape				query	= null;

		public RangeQueryStrategy(IShape query) {
			super();
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
		public void getNextEntry(QuadTree n, QuadTree[] next, boolean[] hasNext) {
			handleQueryLazily(n, hasNext);

			if (!toVisit.isEmpty()) {
				next[0] = toVisit.remove(0);
				hasNext[0] = true;
			} else {
				hasNext[0] = false;
			}
		}

		private void handleQueryLazily(QuadTree n, boolean[] hasNext) {
			MemQTree tree = (MemQTree) n;
			if (!tree.isLeaf()) {
				if (simulator.getMode() == MODE.LAZY) {
					if (n.getChTrees() == null) {
						tree.createChTrees();
					}
					tree.updateU(0);
				} else {
					if (n.getChTrees() == null) {
						return;
					}
				}
				if (n.getCnt() == 0)
					return;
				QuadTree[] chTrees = n.getChTrees();
				ArrayList<QuadEntry> entries = n.getEntries();
				for (int i = 0; i < n.getDivision(); i++) {
					if (entries.get(i) == null)
						continue;
					if (query.contains(chTrees[i].getBoundary())) { // ans
						ArrayList<Tuple> tuples = new ArrayList<Tuple>();
						Set<Integer> ansIds = new HashSet<Integer>();
						if (simulator.getMode() == MODE.REBUILD) {
							RetrieveStrategy qs = new RetrieveStrategy();
							queryStrategy(chTrees[i], qs);
							tuples = qs.getTuples();
							ansIds = qs.getAnsIds();
						} else {
							for (Entry entryL : tree.L.values()) {
								if (chTrees[i].getBoundary().contains(
										entryL.getShape())) {
									tuples.add(entryL.getTuple());
									ansIds.add(entryL.getId());
								}
							}
						}
						voCells.add(new VOCell(ansIds, tuples, (Entry) entries.get(i)));
					} else if (chTrees[i].getBoundary().intersects(query)) { // explore
																				// more
						toVisit.add(chTrees[i]);
					} else { // outside
						voCells.add(new VOCell(-1, null,
								(Entry) entries.get(i)));
					}
				}
			} else {
				if (simulator.getMode() == MODE.LAZY)
					tree.updateU(0); //
				for (Entry entry : tree.L.values()) {
					if (query.contains(entry.getShape())) {
						voCells.add(new VOCell(entry.getId(), entry.getTuple(), entry));
					} else {
						voCells.add(new VOCell(-1, null, entry));
					}
				}
			}

		}
	}

	class RetrieveStrategy implements IQueryStrategyQT {

		private ArrayList<QuadTree>	toVisit	= new ArrayList<QuadTree>();
		ArrayList<Tuple>			tuples	= null;

		public ArrayList<Tuple> getTuples() {
			return tuples;
		}

		public Set<Integer> getAnsIds() {
			Set<Integer> res = new HashSet<Integer>();
			for (Tuple tuple : tuples) {
				res.add(tuple.getId());
			}
			return res;
		}

		/**
		 * 
		 */
		public RetrieveStrategy() {
			tuples = new ArrayList<Tuple>();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see memoryindex.IQueryStrategy#getNextEntry(memoryindex.BinaryTree,
		 * memoryindex.BinaryTree[], boolean[])
		 */
		@Override
		public void getNextEntry(QuadTree n, QuadTree[] next, boolean[] hasNext) {
			MemQTree tree = (MemQTree) n;
			if (tree.isLeaf()) {
				for (Entry entry : tree.L.values()) {
					tuples.add(entry.getTuple());
				}
			} else if (n.getCnt() != 0) {
				QuadTree[] chTrees = n.getChTrees();
				for (int i = 0; i < n.getDivision(); i++) {
					toVisit.add(chTrees[i]);
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

	public boolean isLeaf() {
		return lev == Global.L - 1;
	}

	@Override
	public INDEX_TYPE getType() {
		return INDEX_TYPE.QTree;
	}

	class UpdateEntry {
		OP		op		= null;
		Entry	entry	= null;

		public UpdateEntry(OP op, Entry entry) {
			this.op = op;
			this.entry = entry;
		}

		public OP getOP() {
			return op;
		}

		public Entry getEntry() {
			return entry;
		}

		public int getId() {
			return  entry.getId();
		}

		public int getTS() {
			return entry.getTS();
		}
	}

	@Override
	public void read(DataInputStream ds) {
		if (IO.readBoolean(ds)) {
			int size = IO.readInt(ds);
			setEntries(new ArrayList<QuadEntry>(size));
			for (int i = 0; i < size; ++i) {
				if (IO.readBoolean(ds)) {
					Entry entry = new Entry();
					entry.read(ds);
					setEntry(i, entry);
				} else {
					setEntry(i, null);
				}
			}
		}
		if (isLeaf()) {
			int num = IO.readInt(ds);
			L = new HashMap<Integer, Entry>();
			for (int i = 0; i < num; i++) {
				int id = IO.readInt(ds);
				Entry entry = new Entry();
				L.put(id, entry);
			}
		}
		if (!IO.readBoolean(ds)) {
			MemQTree[] chTrees = (MemQTree[]) getChTrees();
			for (int i = 0; i < chTrees.length; i++) {
				MemQTree memQTree = chTrees[i];
				memQTree.read(ds);
			}
		}
	}

	@Override
	public void write(DataOutputStream ds) {
		if (getEntries() == null) {
			IO.writeBoolean(ds, false);
		} else {
			IO.writeBoolean(ds, true);
			IO.writeInt(ds, getEntries().size());
			for (QuadEntry entry : getEntries()) {
				if (entry == null) {
					IO.writeBoolean(ds, false);
				} else {
					IO.writeBoolean(ds, true);
					((Entry) entry).write(ds);
				}
			}
		}
		if (isLeaf()) {
			IO.writeInt(ds, L.size());
			for (java.util.Map.Entry<Integer, Entry> entry : L.entrySet()) {
				IO.writeInt(ds, entry.getKey());
				entry.getValue().write(ds);
			}
		}
		if (!isLeaf()) {
			IO.writeBoolean(ds, false);
			MemQTree[] chTrees = (MemQTree[]) getChTrees();
			if (chTrees != null && getCnt() > 0) {
				for (int i = 0; i < chTrees.length; i++) {
					chTrees[i].write(ds);
				}
			}
		} else {
			IO.writeBoolean(ds, true);
		}
	}

	@Override
	public ArrayList<IShape> kNN(IShape query, int k) {
		KNNVisitor visitor = new KNNVisitor();
		nearestNeighborQuery(k, query, visitor);
		return visitor.getPoints();
	}
	
	class KNNVisitor implements IVisitorQT {

		ArrayList<IShape> points = new ArrayList<IShape>();
		@Override
		public void visitEntry(QuadEntry entry) {
			points.add(entry.getShape());
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
		queue.add(new SLEntry(this));
		
		ArrayList<IShape> res = new ArrayList<IShape>();
		
		while (!queue.isEmpty()) {
			SLEntry first = queue.poll();
			
			if (first.tree != null) {
				QuadTree tree = first.tree;
				if (tree.getChTrees() == null) {
					for (int i = 0; i < tree.getCnt(); ++i) {
						if (!dominate(res, tree.getEntry(i).getShape())) queue.add(new SLEntry(tree.getEntry(i)));
					}
				} else {
					for (int i = 0; i < tree.getChTrees().length; ++i) {
						if (tree.getChTree(i) != null) {
							if (!dominate(res, tree.getChTree(i).getBoundary())) queue.add(new SLEntry(tree.getChTree(i)));
						}
					}
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
		QuadTree tree = null;
		QuadEntry entry = null;
		double minDist = 0;
		
		public SLEntry(QuadTree _tree) {
			tree = _tree;
			minDist = 0;
			double[] low = tree.getBoundary().m_pLow;
			for (int i = 0; i < low.length; ++i) {
				minDist += low[i];
			}
		}
		
		public SLEntry(QuadEntry _entry) {
			entry = _entry;
			minDist = 0;
			double[] low = _entry.getShape().getMBR().m_pLow;
			for (int i = 0; i < low.length; ++i) {
				minDist += low[i];
			}
		}

		@Override
		public int compareTo(SLEntry o) {
			if (minDist < o.minDist) return -1;
			else if (minDist > o.minDist) return 1;
			else return 0;
		}
	}

	@Override
	public byte[] toBytes() {
		return IO.toBytes(this);
	}
}
