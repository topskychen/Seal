/**
 * 
 */
package index;

import index.BinarySearchTree.RangeQueryStrategy;
import io.RW;

import java.util.ArrayList;
import java.util.HashMap;

import spatialindex.Point;
import spatialindex.Region;
import utility.Constants;
import utility.Query;
import utility.Tuple;
import utility.VOCell;
import memoryindex.BinaryTree;
import memoryindex.IQueryStrategyBT;
import memoryindex.IQueryStrategyQT;
import memoryindex.QuadTree;

/**
 * @author chenqian
 *
 */
public class MemQTree extends QuadTree implements SearchIndex {

	Entry entry = null;	
	
	/**
	 * @param capacity
	 * @param boundary
	 */
	public MemQTree(int capacity, Region boundary) {
		super(capacity, boundary);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<VOCell> rangeQuery(Query query) {
		// TODO Auto-generated method stub
		RangeQueryStrategy rangeQueryStrategy = new RangeQueryStrategy(query);
		queryStrategy(this, rangeQueryStrategy);
		return rangeQueryStrategy.getVOCells();
	}

	@Override
	public void buildIndex(ArrayList<Entry> entries) {
		// TODO Auto-generated method stub
		for (int i = 0; i < entries.size(); i ++) {
			Entry entry = entries.get(i);
			Point p = new Point(entry.getLB().doubleCoords());
			this.insert(p, entry);
		}
		buildIndex(this);
	}
	
	public Entry buildIndex(QuadTree tree) {
		if (tree.getCnt() == 0) return null;
		ArrayList<Point> points = tree.getPoints();
		if (points == null) {
			QuadTree[] chTree = tree.getChTrees();
			ArrayList<Entry> entries = new ArrayList<Entry>();
			ArrayList<RW> values = new ArrayList<RW>();
			for (int i = 0; i < getDim(); i ++) {
				Entry entry = buildIndex(chTree[i]);
				values.add(entry);
				if (entry != null) {
					entries.add(entry);
				}
			}
			tree.setValue(values);
			return new Entry(-1, entries.toArray(new Entry[0]));
		} else {
			ArrayList<RW> values = tree.getValues();
			Entry[] entries = new Entry[values.size()];
			for (int i = 0; i < points.size(); i ++) {
				entries[i] = (Entry) values.get(i);
			}
			return new Entry(-1, entries);
		}
	}
	
	public ArrayList<Integer> getPath(spatialindex.Point p) {
		ArrayList<Integer> path = new ArrayList<Integer>();
		getPath(path, p, getBoundary(), 0, 0);
		return path;
	}
	
	public void getPath(ArrayList<Integer> path, spatialindex.Point p, Region boundary, int level, int id) {
		if (level == Constants.L) {
			return;
		}
		path.add(id);
		Region[] regions = subDivide(boundary);
		for (int i = 0; i < getDim(); i ++) {
			if (regions[i].contains(p)) {
				getPath(path, p, regions[i], level + 1, (id << 2) | i);
			}
		}
	}
	
	class RangeQueryStrategy implements IQueryStrategyQT {

		private ArrayList<QuadTree> toVisit = new ArrayList<QuadTree>();
		private ArrayList<VOCell> 	voCells = new ArrayList<VOCell>();
		private Region 				query 	= null;
		
		
		public RangeQueryStrategy(Query query) {
			super();
			this.query = new Region(query.getLB().doubleCoords(), query.getHB().doubleCoords());
		}

		public ArrayList<VOCell> getVOCells() {
			return voCells;
		}

		@Override
		public void getNextEntry(QuadTree n, QuadTree[] next,
				boolean[] hasNext) {
			// TODO Auto-generated method stub
		
			ArrayList<Point> points = n.getPoints();
			if (points != null) {
				ArrayList<RW> values = n.getValues();
				for (int i = 0; i < points.size(); i ++) {
					Entry entry = (Entry) values.get(i);
					ArrayList<Tuple> tuples = new ArrayList<Tuple>();
					tuples.add(entry.getTuple());
					voCells.add(new VOCell(tuples, entry));
				}
			} else if(n.getCnt() != 0) {
				QuadTree[] chTrees = n.getChTrees();
				ArrayList<RW> values = n.getValues();
				for (int i = 0; i < n.getDim(); i ++) {
					if (values.get(i) == null) continue;
					if (query.contains(chTrees[i].getBoundary())) {
						RetrieveStrategy qs = new RetrieveStrategy();
						queryStrategy(chTrees[i], qs);
						voCells.add(new VOCell(qs.getTuples(), (Entry) values.get(i)));
					} else if (chTrees[i].getBoundary().intersects(query)) {
						toVisit.add(chTrees[i]);
					} else {
						voCells.add(new VOCell(null, (Entry) values.get(i)));
					}
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


}
