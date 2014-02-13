/**
 * 
 */
package index;

import io.RW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import spatialindex.IShape;
import spatialindex.Point;
import spatialindex.Region;
import utility.Constants;
import utility.Constants.MODE;
import utility.Constants.OP;
import utility.Tuple;
import utility.VOCell;
import memoryindex.IQueryStrategyQT;
import memoryindex.QuadTree;

/**
 * 
 * For lazy update.
 * L is short for lastest updated.
 * U is short for un-updated ones. 
 * @author chenqian
 *
 */
public class MemQTree extends QuadTree implements SearchIndex {

	private	int 							lev			= 0;
	public	HashMap<Integer, Entry>		 	L 			= null;
	public	HashMap<Integer, UpdateEntry> 	U 			= null;
	
	
	/**
	 * @param capacity
	 * @param boundary
	 */
	public MemQTree(int capacity, Region boundary, int lev, int id) {
		super(capacity, boundary);
		this.lev 	= lev;
		L 			= new HashMap<Integer, Entry>();
		U 			= new HashMap<Integer, UpdateEntry>();
		if (Constants.G_MODE != MODE.REBUILD && !isLeaf()) {
			ArrayList<RW> values = new ArrayList<RW>();
			for (int i = 0; i < 4; i ++) {
				values.add(null);
			}
			setValues(values);
		}		
		setId(id);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<VOCell> rangeQuery(IShape query) {
		// TODO Auto-generated method stub
		RangeQueryStrategy rangeQueryStrategy = new RangeQueryStrategy(query);
		queryStrategy(this, rangeQueryStrategy);
		return rangeQueryStrategy.getVOCells();
	}

	@Override
	public void buildIndex(ArrayList<Entry> entries) {
		// TODO Auto-generated method stub
		if (Constants.G_MODE != MODE.REBUILD) {
			for (Entry entry : entries) {				
				if (Constants.G_MODE == MODE.LAZY) {
					putU(entry.getId(), new UpdateEntry(OP.ADD, entry));
				} else {
					replace(entry);
				}
			}
		} else{			
			for (Entry entry : entries) {
				this.insert((Point) entry.getShape(), entry);
			}
			buildIndex(this, null, 0);
		}
	}
	
	public void putU (int id, UpdateEntry entry){
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
		if (entry == null) return -1;
		for (int i = 0; i < getDim(); i ++) {
			if (chTrees[i].contains(entry)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Add a entry
	 * @param chTrees
	 * @param key
	 * @param iU
	 * @param entryU
	 */
	public void addEntry(MemQTree[] chTrees, int key, int iU, UpdateEntry entryU) {
		if (Constants.G_MODE == MODE.LAZY)
			chTrees[iU].putU(key, entryU); 				// push down
		Entry entry = (Entry) getValue(iU);
		if (entry == null) { 						// if is empty
			setValue(iU, (RW) entryU.getEntry().clone());
		} else { 									// otherwise update the value with entry in U
			entry.update(null, entryU.getEntry());
		}
		L.put(key, entryU.getEntry()); 				// update the latest updated
	}
	
	public void delEntry(MemQTree[] chTrees, int key, int iL, Entry entryL) {
		if (iL == -1) return;
		if (Constants.G_MODE == MODE.LAZY)
			chTrees[iL].putU(key, new UpdateEntry(OP.DEL, entryL)); 	// push down
		Entry entry = (Entry) getValue(iL);
		if (entry == null) return;
		else entry.update(entryL, null);
		if (entry.getNO() == 0) { // this node has no values
			setValue(iL, null);
		}
		L.remove(key);
	}
	
	/**
	 * The chTrees should not be empty.
	 * @param chTrees
	 */
	public void updateChTree(MemQTree[] chTrees) {
		for (java.util.Map.Entry<Integer, UpdateEntry> entryInU : U.entrySet()) {
			int 		key 	= entryInU.getKey();
			UpdateEntry entryU 	= entryInU.getValue();
			Entry 		entryL 	= L.get(key);
			int			iU 		= locate(chTrees, entryU.getEntry()); 
			int 		iL 		= locate(chTrees, entryL);
			
			delEntry(chTrees, key, iL, entryL);
			if (entryU.getOP() == OP.ADD) {
				addEntry(chTrees, key, iU, entryU);
			} else {
				chTrees[iU].putU(key, entryU);
			}
		}
	}
	
	
	public void updateU() {
		if (U.isEmpty()) return;
		if (isLeaf()) { // replace the L with U
			for (java.util.Map.Entry<Integer, UpdateEntry> entry : U.entrySet()) {
				if (entry.getValue().getOP() == OP.DEL) {
					if (L.containsKey(entry.getKey())) L.remove(entry.getKey());
				} else {
					L.put(entry.getKey(), entry.getValue().getEntry());
				}
			}
			setCnt(L.size());
		} else {
			updateChTree((MemQTree[]) getChTrees());
			int cnt = 0;
			for (int i = 0; i < getDim(); i ++) {
				Entry entry = (Entry) getValue(i);
				if (entry != null) {
					cnt += entry.getNO();					
				}
			}
			setCnt(cnt);
		}
		U.clear();
	}
	
	public void pushU() {
		if (isLeaf()) {
			throw new IllegalStateException("When it arrives at leaf, no push is needed!");
		} else {
//			if (getChTrees() == null) {
//				createChTrees();
//			}
			MemQTree[] chTrees = (MemQTree[]) getChTrees();
			for (UpdateEntry entryU : U.values()) {
				for (int i = 0; i < getDim(); i ++) {
					if (chTrees[i].contains(entryU.getEntry())) {
						chTrees[i].putU(entryU.getId(), entryU);
					}
				}
			}
		}
	}
	
	public void createChTrees() {
		MemQTree[] chTrees = new MemQTree[4];
		Region[] regions = subDivide(getBoundary());
		for (int i = 0; i < 4; i ++) {
			chTrees[i] = new MemQTree(getCapacity(), regions[i], lev + 1, (int) (((getId() + 1) << 2) + i - 1));
		}
		setChTrees(chTrees);
	}
	
	
	
	public boolean contains(Entry entry) {
		return this.getBoundary().contains(entry.getShape());
	}
	
	public Entry buildIndex(QuadTree tree, HashSet<Long> modified, int lev) {
		if (tree.getCnt() == 0) return null;
		ArrayList<Point> points = tree.getPoints();
		if (points == null) {
			QuadTree[] chTree = tree.getChTrees();
			ArrayList<Entry> entries = new ArrayList<Entry>();
			ArrayList<RW> values = new ArrayList<RW>();
			for (int i = 0; i < getDim(); i ++) {
				Entry entry = null;
				if (modified == null || 
						modified.contains(chTree[i].getId()) == true) {
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
			for (int i = 0; i < points.size(); i ++) {
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
	
	public void getPath(ArrayList<Integer> path, spatialindex.Point p, Region boundary, int level, int id) {
		if (level == Constants.L) {
			return;
		}
		path.add(id - 1);
		Region[] regions = subDivide(boundary);
		for (int i = 0; i < getDim(); i ++) {
			if (regions[i].contains(p)) {
				getPath(path, p, regions[i], level + 1, (id << 2) + i);
			}
		}
	}
	
	/**
	 * Insert a point, if exists then replace
	 * @param entry
	 */
	public void replace(Entry entry) {
		if (L.containsKey(entry.getId())) {
			Entry oldEntry = L.get(entry.getId());
			if (delete(oldEntry) == false) {
				throw new IllegalStateException("A entry is not deleted.");
			}
		}
		if (insert(entry) == false) {
			throw new IllegalStateException("A entry is not added.");
		}
		
	}
	
	public boolean insert(Entry entry) {
		if (!contains(entry)) return false;
		if (isLeaf()) {
			L.put(entry.getId(), entry);
			setCnt(L.size());
			return true;
		} else {
			if (getChTrees() == null) {
				createChTrees();
			}
			MemQTree[] chTrees = (MemQTree[]) getChTrees(); 
			for (int i = 0; i < getDim(); i ++) {
				if (chTrees[i].contains(entry)) {
					addEntry(chTrees, entry.getId(), i, new UpdateEntry(null, entry));
					setCnt(L.size());
					if (chTrees[i].insert(entry)) return true;
					else return false;
				}
			}
			setCnt(L.size());
			return false;
		}
	}
	
	public boolean delete(Entry entry) {
		if (!contains(entry)) return false;
		if (isLeaf()) {
			L.remove(entry.getId());
			setCnt(L.size());
			return true;
		} else {
			if (getChTrees() == null) {
				throw new IllegalStateException("The chTrees cannot be null.");
			}
			MemQTree[] chTrees = (MemQTree[]) getChTrees();
			for (int i = 0; i < getDim(); i ++) {
				if (chTrees[i].contains(entry)) {
					delEntry(chTrees, entry.getId(), i, entry);
					setCnt(L.size());
					if (chTrees[i].delete(entry)) return true;
					else return false;
				}
			}
			return false;
		}
	}
	
//	public void reBuild(ArrayList<QuadTree> path) {
//		HashSet<Long> modified = new HashSet<Long>();
//		for (QuadTree tree : path) {
//			if (tree != null && tree.getId() != -1) {
//				modified.add(tree.getId());
//			}
//		}
//		buildIndex(this, modified, 0);
//	}
	
	
	
	class RangeQueryStrategy implements IQueryStrategyQT {

		private ArrayList<QuadTree> toVisit = new ArrayList<QuadTree>();
		private ArrayList<VOCell> 	voCells = new ArrayList<VOCell>();
		private IShape 				query 	= null;
		
		
		public RangeQueryStrategy(IShape query) {
			super();
			this.query = query;
		}

		public ArrayList<VOCell> getVOCells() {
			return voCells;
		}
		
		public void handleQuery(QuadTree n, boolean[] hasNext) {
			ArrayList<Point> points = n.getPoints();
			if (points != null) {
				ArrayList<RW> values = n.getValues();
				for (int i = 0; i < points.size(); i ++) {
					Entry entry = (Entry) values.get(i);
					if (query.contains(points.get(i))) {
						voCells.add(new VOCell(entry.getTuple(), entry));
					} else {
						voCells.add(new VOCell(new ArrayList<Tuple>(), entry));
					}
				}
			} else if(n.getCnt() != 0) {
				QuadTree[] 		chTrees 	= n.getChTrees();
				ArrayList<RW> 	values 		= n.getValues();
				for (int i = 0; i < n.getDim(); i ++) {
					if (values.get(i) == null) continue;
					if (query.contains(chTrees[i].getBoundary())) {
						RetrieveStrategy qs = new RetrieveStrategy();
						queryStrategy(chTrees[i], qs);
						voCells.add(new VOCell(qs.getTuples(), (Entry) values.get(i)));
					} else if (chTrees[i].getBoundary().intersects(query)) {
						toVisit.add(chTrees[i]);
					} else {
						Entry entry = (Entry) values.get(i);
						if (entry.getTuple().getComPre().length == 0) { // should never happen
							RetrieveStrategy qs = new RetrieveStrategy();
							queryStrategy(chTrees[i], qs);
							voCells.add(new VOCell(qs.getTuples(), (Entry) values.get(i)));
						} else {
							voCells.add(new VOCell(new ArrayList<Tuple>(), entry));
						}
					}
				}
			}
		}

		@Override
		public void getNextEntry(QuadTree n, QuadTree[] next,
				boolean[] hasNext) {
			if (Constants.G_MODE != MODE.REBUILD) {
				handleQueryLazily(n, hasNext);
			} else {
				handleQuery(n, hasNext);				
			}
			
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
				if (Constants.G_MODE == MODE.LAZY) {
					if (n.getChTrees() == null) {
						tree.createChTrees();
					}
					tree.updateU();
				} else {
					if (n.getChTrees() == null) {
						return;
					}
				}
				if (n.getCnt() == 0) return;
				QuadTree[] 		chTrees 	= n.getChTrees();
				ArrayList<RW> 	values 		= n.getValues();
				for (int i = 0; i < n.getDim(); i ++) {
					if (values.get(i) == null) continue;
					if (query.contains(chTrees[i].getBoundary())) { // ans
						ArrayList<Tuple> tuples = new ArrayList<Tuple>();
						for(Entry entryL : tree.L.values()) {
							if (chTrees[i].getBoundary().contains(entryL.getShape()))
								tuples.add(entryL.getTuple());
						}
						voCells.add(new VOCell(tuples, (Entry) values.get(i))); 
					} else if (chTrees[i].getBoundary().intersects(query)) { // explore more
						toVisit.add(chTrees[i]);
					} else { //outside
						voCells.add(new VOCell(new ArrayList<Tuple>(), (Entry) values.get(i)));
					}
				}
			} else {
				if (Constants.G_MODE == MODE.LAZY) tree.updateU(); // 
				for (Entry entry : tree.L.values()) {
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

		private ArrayList<QuadTree> 	toVisit = new ArrayList<QuadTree>();
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
		public void getNextEntry(QuadTree n, QuadTree[] next, boolean[] hasNext) {
			// TODO Auto-generated method stub
			ArrayList<Point> points = n.getPoints();
			if (points != null) {
				ArrayList<RW> values = n.getValues();
				for (int i = 0; i < points.size(); i ++) {
					tuples.add(((Entry) values.get(i)).getTuple());
				}
			} else if (n.getCnt() != 0) {
				QuadTree[] chTrees = n.getChTrees();
				for (int i = 0; i < n.getDim(); i ++) {
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
		return lev == Constants.L - 1;
	}
	
	@Override
	public INDEX_TYPE getType() {
		// TODO Auto-generated method stub
		return INDEX_TYPE.QTree;
	}

	class UpdateEntry {
		OP		op 		= null;
		Entry 	entry 	= null;
		
		public UpdateEntry(OP op, Entry entry) {
			this.op 	= op;
			this.entry 	= entry;
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

}
