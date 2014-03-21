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

import memoryindex.IQueryStrategyQT;
import memoryindex.QuadTree;
import party.DataOwner;
import spatialindex.IShape;
import spatialindex.Point;
import spatialindex.Region;
import utility.Global;
import utility.Global.MODE;
import utility.Global.OP;
import utility.StatisticsUpdate;
import utility.Tuple;
import utility.VOCell;

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
	static HashMap<Integer, Entry>			leafEntries		= new HashMap<Integer, Entry>();	// for
																								// rebuild
	static HashMap<Integer, Entry>			innerEntries	= new HashMap<Integer, Entry>();	// for
																								// rebuild

	/**
	 * @param capacity
	 * @param boundary
	 */
	public MemQTree(int capacity, Region boundary, int lev, int id) {
		super(capacity, boundary);
		this.lev = lev;
		L = new HashMap<Integer, Entry>();
		U = new HashMap<Integer, UpdateEntry>();
		// if (Constants.G_MODE != MODE.REBUILD && !isLeaf()) {
		if (!isLeaf()) {
			ArrayList<RW> values = new ArrayList<RW>();
			for (int i = 0; i < 4; i++) {
				values.add(null);
			}
			setValues(values);
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
	public void buildIndex(ArrayList<DataOwner> owners,
			ArrayList<Entry> entries, StatisticsUpdate statU) {
		Global.G_TIMER.reset();
		for (Entry entry : entries) {
			if (Global.G_MODE == MODE.LAZY) {
				putU(entry.getId(), new UpdateEntry(OP.ADD, entry));
			} else {
				replace(entry);
			}
		}
		Global.G_TIMER.stop();
		if (entries.size() != Global.TOTN) {
			statU.append(Global.G_TIMER.timeElapseinMs(), 0);
		} else {
			pushU(0);
		}
		if (Global.G_MODE == MODE.LAZY)
			pushU(Global.BUFFER_SIZE);
		// if (Constants.G_MODE != MODE.REBUILD) {
		// } else{
		// for (Entry entry : entries) {
		// this.insert((Point) entry.getShape(), entry);
		// }
		// buildIndex(this, null, 0);
		// }
	}

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

	public int locate(MemQTree[] chTrees, Entry entry) {
		if (entry == null)
			return -1;
		for (int i = 0; i < getDim(); i++) {
			if (chTrees[i].contains(entry)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Add a entry
	 * 
	 * @param chTrees
	 * @param key
	 * @param iU
	 * @param entryU
	 */
	public void addEntry(MemQTree[] chTrees, int key, int iU, UpdateEntry entryU) {
		if (Global.G_MODE == MODE.LAZY)
			chTrees[iU].putU(key, entryU); // push down
		Entry entry = (Entry) getValue(iU);
		if (entry == null) { // if is empty
			setValue(iU, (RW) entryU.getEntry().clone());
		} else { // otherwise update the value with entry in U
			entry.update(null, entryU.getEntry());
		}
		L.put(key, entryU.getEntry()); // update the latest updated
	}

	public void delEntry(MemQTree[] chTrees, int key, int iL, Entry entryL) {
		if (iL == -1)
			return;
		if (Global.G_MODE == MODE.LAZY)
			chTrees[iL].putU(key, new UpdateEntry(OP.DEL, entryL)); // push down
		Entry entry = (Entry) getValue(iL);
		if (entry == null)
			return;
		else
			entry.update(entryL, null);
		if (entry.getNO() == 0) { // this node has no values
			setValue(iL, null);
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
	 * Update an node
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
			for (int i = 0; i < getDim(); i++) {
				Entry entry = (Entry) getValue(i);
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
				for (int i = 0; i < getDim(); i++) {
					chTrees[i].pushU(bufferSize);
				}
			}
		} else {
			updateU(bufferSize);
		}
	}

	// public void pushU() {
	// if (isLeaf()) {
	// throw new IllegalStateException(
	// "When it arrives at leaf, no push is needed!");
	// } else {
	// // if (getChTrees() == null) {
	// // createChTrees();
	// // }
	// MemQTree[] chTrees = (MemQTree[]) getChTrees();
	// for (UpdateEntry entryU : U.values()) {
	// for (int i = 0; i < getDim(); i++) {
	// if (chTrees[i].contains(entryU.getEntry())) {
	// chTrees[i].putU(entryU.getId(), entryU);
	// }
	// }
	// }
	// }
	// }

	public void createChTrees() {
		MemQTree[] chTrees = new MemQTree[4];
		Region[] regions = subDivide(getBoundary());
		for (int i = 0; i < 4; i++) {
			chTrees[i] = new MemQTree(getCapacity(), regions[i], lev + 1,
					(int) (((getId() + 1) << 2) + i - 1));
		}
		setChTrees(chTrees);
	}

	public boolean contains(Entry entry) {
		return this.getBoundary().contains(entry.getShape());
	}

	public Entry buildIndex(QuadTree tree, HashSet<Long> modified, int lev) {
		if (tree.getCnt() == 0)
			return null;
		ArrayList<Point> points = tree.getPoints();
		if (points == null) {
			QuadTree[] chTree = tree.getChTrees();
			ArrayList<Entry> entries = new ArrayList<Entry>();
			ArrayList<RW> values = new ArrayList<RW>();
			for (int i = 0; i < getDim(); i++) {
				Entry entry = null;
				if (modified == null
						|| modified.contains(chTree[i].getId()) == true) {
					//
					entry = buildIndex(chTree[i], modified, lev + 1);
				} else {
					entry = (Entry) tree.getValue(i);
				}
				values.add(entry);
				if (entry != null) {
					entries.add(entry);
				}
			}
			tree.setValues(values);
			return new Entry(-1, entries.toArray(new Entry[0]), lev);
		} else {
			ArrayList<RW> values = tree.getValues();
			Entry[] entries = new Entry[values.size()];
			for (int i = 0; i < points.size(); i++) {
				entries[i] = (Entry) values.get(i);
			}
			return new Entry(-1, entries, lev);
		}
	}

	public ArrayList<Integer> getPath(Point p) {
		ArrayList<Integer> path = new ArrayList<Integer>();
		getPath(path, p, getBoundary(), 0, 1);
		return path;
	}

	public void getPath(ArrayList<Integer> path, spatialindex.Point p,
			Region boundary, int level, int id) {
		if (level == Global.L) {
			return;
		}
		path.add(id - 1);
		Region[] regions = subDivide(boundary);
		boolean found = false;
		for (int i = 0; i < getDim(); i++) {
			if (regions[i].contains(p)) {
				getPath(path, p, regions[i], level + 1, (id << 2) + i);
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
				throw new IllegalStateException("A entry is not deleted.");
			}
		}
		if (Global.G_MODE == MODE.REBUILD) {
			leafEntries.put(entry.getId(), entry);
		}
		if (insert(entry) == false) {
			throw new IllegalStateException("A entry is not added.");
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
			for (int i = 0; i < getDim(); i++) {
				if (chTrees[i].contains(entry)) {
					addEntry(chTrees, entry.getId(), i, new UpdateEntry(null,
							entry));
					if (Global.G_MODE == MODE.REBUILD) {
						innerEntries.put((int) chTrees[i].getId(),
								(Entry) getValue(i));
					}
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
			if (getChTrees() == null) {
				throw new IllegalStateException("The chTrees cannot be null.");
			}
			MemQTree[] chTrees = (MemQTree[]) getChTrees();
			for (int i = 0; i < getDim(); i++) {
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
		private IShape				query	= null;

		public RangeQueryStrategy(IShape query) {
			super();
			this.query = query;
		}

		public ArrayList<VOCell> getVOCells() {
			return voCells;
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
				if (Global.G_MODE == MODE.LAZY) {
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
				ArrayList<RW> values = n.getValues();
				for (int i = 0; i < n.getDim(); i++) {
					if (Global.G_MODE == MODE.REBUILD) {
						Entry entry = innerEntries.get(chTrees[i]);
						if (entry != null)
							entry.getId();
					}
					if (values.get(i) == null)
						continue;
					if (query.contains(chTrees[i].getBoundary())) { // ans
						ArrayList<Tuple> tuples = new ArrayList<Tuple>();
						if (Global.G_MODE == MODE.REBUILD) {
							RetrieveStrategy qs = new RetrieveStrategy();
							queryStrategy(chTrees[i], qs);
							tuples = qs.getTuples();
						} else {
							for (Entry entryL : tree.L.values()) {
								if (chTrees[i].getBoundary().contains(
										entryL.getShape())) {
									tuples.add(entryL.getTuple());
								}
							}
						}
						voCells.add(new VOCell(tuples, (Entry) values.get(i)));
					} else if (chTrees[i].getBoundary().intersects(query)) { // explore
																				// more
						toVisit.add(chTrees[i]);
					} else { // outside
						if (Global.G_MODE == MODE.REBUILD) {
							voCells.add(new VOCell(new ArrayList<Tuple>(),
									innerEntries.get((int) chTrees[i].getId())));
						} else {
							voCells.add(new VOCell(new ArrayList<Tuple>(),
									(Entry) values.get(i)));
						}
					}
				}
			} else {
				if (Global.G_MODE == MODE.LAZY)
					tree.updateU(0); //
				for (Entry entry : tree.L.values()) {
					if (Global.G_MODE == MODE.REBUILD) {
						entry = leafEntries.get(entry.getId());
					}
					if (query.contains(entry.getShape())) {
						voCells.add(new VOCell(entry.getTuple(), entry));
					} else {
						voCells.add(new VOCell(new ArrayList<Tuple>(), entry));
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
		public void getNextEntry(QuadTree n, QuadTree[] next, boolean[] hasNext) {
			// TODO Auto-generated method stub
			MemQTree tree = (MemQTree) n;
			if (tree.isLeaf()) {
				for (Entry entry : tree.L.values()) {
					tuples.add(leafEntries.get(entry.getId()).getTuple());
				}
			} else if (n.getCnt() != 0) {
				QuadTree[] chTrees = n.getChTrees();
				for (int i = 0; i < n.getDim(); i++) {
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
		// TODO Auto-generated method stub
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
			return entry.getId();
		}

		public int getTS() {
			return entry.getTS();
		}
	}

	@Override
	public void read(DataInputStream ds) {
		int num = IO.readInt(ds);
		L = new HashMap<Integer, Entry>();
		for (int i = 0; i < num; i++) {
			int id = IO.readInt(ds);
			Entry entry = new Entry();
			L.put(id, entry);
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
		IO.writeInt(ds, L.size());
		for (java.util.Map.Entry<Integer, Entry> entry : L.entrySet()) {
			IO.writeInt(ds, entry.getKey());
			entry.getValue().write(ds);
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

}
