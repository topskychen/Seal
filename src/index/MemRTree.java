/**
 * 
 */
package index;

import io.IO;
import io.RW;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import party.DataOwner;
import rtree.Node;
import rtree.RTree;
import rtree.Records;
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
import timer.Timer;
import utility.Global;
import utility.Global.MODE;
import utility.Seal;
import utility.StatisticsUpdate;
import utility.Tuple;

/**
 * @author chenqian
 * 
 */
public class MemRTree extends RTree implements SearchIndex, RW {

	HashMap<Integer, Entry>	innerEntries	= null;
//	HashMap<Integer, Entry>	leafEntries		= null;
	Entry[]					leafEntries		= null;
	HashSet<Integer>		rebuildIds		= null;
	Timer					timer			= null;

	public MemRTree(PropertySet ps, IStorageManager sm) {
		super(ps, sm);
		innerEntries = new HashMap<Integer, Entry>(Global.TOTN);
//		leafEntries = new HashMap<Integer, Entry>((int)(Global.TOTN * 1.2));
		leafEntries = new Entry[Global.TOTN];
		timer = new Timer();
		// TODO Auto-generated constructor stub
	}

	public static MemRTree createTree() {
		IStorageManager sm = new MemoryStorageManager();
		IBuffer buffer = new RandomEvictionsBuffer(sm, 143000, false); // no
																		// buffer
																		// due
																		// to no
																		// page
																		// reuse
		PropertySet ps = new PropertySet();
		ps.setProperty("FillFactor", new Double(0.7));
		ps.setProperty("IndexCapacity", new Integer(Global.F));
		ps.setProperty("LeafCapacity", new Integer(Global.F));
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
	 * 
	 * @param entry
	 */
	public void replace(Entry entry) {

		if (leafEntries[entry.getId()] != null) {
			Entry oldEntry = leafEntries[entry.getId()];
			if (!deleteData(oldEntry.getShape(), oldEntry.getId())) {
				System.out.println("fail delete");
			}
		}
		insertData(null, entry.getShape(), entry.getId());
		if (leafEntries[entry.getId()] != null) {
			Entry recEntry = leafEntries[entry.getId()];
			recEntry.setShape(entry.getShape());
			recEntry.setTS(entry.getTS());
		} else {
			leafEntries[entry.getId()] = entry;
		}
	}

	@Override
	public void buildIndex(ArrayList<DataOwner> owners,
			ArrayList<Entry> entries, StatisticsUpdate statU) {
		if (Global.G_MODE != MODE.REBUILD) {
			if (Global.G_MODE == MODE.UPDATE || Global.G_MODE == MODE.LOOSE) {
				rebuildIds = new HashSet<Integer>();
				HashSet<Integer> reCalcIds = new HashSet<Integer>();
				Records records = getRecords();
				records.clear();
				int outOfBound = 0;
				for (Entry entry : entries) {
					if (Global.G_MODE == MODE.LOOSE) {
						if (leafEntries[entry.getId()] != null) {
							Point p = (Point) entry.getShape();
							Region region = new Region(new double[] {
									p.getCoord(0) - Global.REGION_L,
									p.getCoord(1) - Global.REGION_L },
									new double[] {
											p.getCoord(0) + Global.REGION_L,
											p.getCoord(1) + Global.REGION_L });
							entry.setShape(region);
						} else {
							Entry oldEntry = leafEntries[entry.getId()];
							if (!oldEntry.getShape().contains(entry.getShape())) { // out
								Point p = (Point) entry.getShape();
								Region region = new Region(
										new double[] {
												p.getCoord(0) - Global.REGION_L,
												p.getCoord(1) - Global.REGION_L },
										new double[] {
												p.getCoord(0) + Global.REGION_L,
												p.getCoord(1) + Global.REGION_L });
								entry.setShape(region);
								outOfBound++;
							} else {
								entry.setShape(oldEntry.getShape());
							}
						}
					}
					replace(entry);
					reCalcIds.add(entry.getId());
				}
				timer.reset();
				for (int i = 0; i < outOfBound; i++) {
					Seal.getSample(i);
				}
				timer.stop();
				if (entries.size() != Global.TOTN) {
					statU.appendDOCPU(timer.timeElapseinMs());
				}
				for (Integer id : records.getVisitedIds()) {
					rebuildIds.add(id);
				}
				// ArrayList<Integer> reCalcIds = records.getDataIds(this);
				for (Entry entry : leafEntries) {
					if (entry == null) continue;
					int[] comPre = DataOwner.comPre(this, entry.getShape(),
							entry.getId());
					if (!Arrays.equals(comPre, entry.getComPre())
							|| entry.getComPre() == null) {
						reCalcIds.add(entry.getId());
					}
				}
				timer.reset();
				int bandWidth = 0;
				for (Integer reCalcId : reCalcIds) {
					Entry entry = leafEntries[reCalcId];
					int[] comPre = DataOwner.comPre(this, entry.getShape(),
							entry.getId());
					entry.setComPre(comPre);
					// entry.setTuple(new Tuple(entry.getId(), entry.getShape(),
					// entry.getTS(), comPre, INDEX_TYPE.RTree));
					entry.setSeal(new Seal(entry.getTuple(), owners.get(
							entry.getId()).getSS(entry.getTS())));
					bandWidth += IO.toBytes(entry).length;
					ArrayList<Integer> path = new ArrayList<Integer>();
					getPath(getRootId(), entry.getShape(), entry.getId(), path);
					rebuildIds.addAll(path);
				}
				rebuildIds.add(getRootId());
				buildIndex(getRootId());
				rebuildIds.clear();
				rebuildIds = null;
				timer.stop();
				if (entries.size() != Global.TOTN) {
					statU.append(timer.timeElapseinMs(), bandWidth);
					statU.appendNum(entries.size(), reCalcIds.size());
				}
			} else {
				throw new IllegalStateException("This mode is not supported");
			}
		} else {
//			timer.reset();
			for (int i = 0; i < entries.size(); i++) {
				Entry entry = entries.get(i);
//				timer.resume();
				insertData(null, entry.getShape(), entry.getId());
//				timer.pause();
				leafEntries[entry.getId()]= entry;
			}
			buildIndex(getRootId());
//			timer.stop();
//			System.out.println("R building index : " + timer.timeElapseinMs() + "ms");
		}
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
				entries[i] = leafEntries[cId];
			}
		}
		Entry entry = new Entry(id, entries, -1);
		entry.setShape(node.getShape());
		innerEntries.put(id, entry);
		return;
	}

	public boolean getPath(int nodeId, IShape p, int id, ArrayList<Integer> path) {
		Node node = readNode(nodeId);
		for (int i = 0; i < node.getChildrenCount(); i++) {
			if (node.getLevel() != 0) {
				if (node.getChildShape(i).contains(p)) {
					if (getPath(node.getChildIdentifier(i), p, id, path)) {
						path.add(node.getChildIdentifier(i));
						return true;
					}
				}
			} else {
				if (node.getChildIdentifier(i) == id) {
					// path.add(id);
					return true;
				}
			}
		}
		return false;
	}

	class RangeQueryStrategy implements IQueryStrategy {

		private ArrayList<Integer>	toVisit	= new ArrayList<Integer>();
		private ArrayList<VOCell>	voCells	= new ArrayList<VOCell>();
		private IShape				query	= null;

		public RangeQueryStrategy(IShape query) {
			this.query = query;
		}

		public ArrayList<VOCell> getVOCells() {
			return voCells;
		}

		@Override
		public void getNextEntry(IEntry e, int[] nextEntry, boolean[] hasNext) {

			Node node = (Node) e;

			for (int i = 0; i < node.getChildrenCount(); i++) {
				Region shape = (Region) node.getChildShape(i);
				Integer cId = node.getChildIdentifier(i);
				if (query.contains(shape)) {
					if (node.isLeaf()) {
						Entry entry = leafEntries[cId];
						voCells.add(new VOCell(entry.getTuple(), entry));
					} else {
						Entry entry = innerEntries.get(cId);
						RetrieveStrategy rs = new RetrieveStrategy();
						queryStrategy(cId, rs);
						voCells.add(new VOCell(rs.getTuples(), entry));
					}
				} else if (!query.intersects(shape)) {
					if (node.isLeaf()) {
						voCells.add(new VOCell(new ArrayList<Tuple>(),
								leafEntries[cId]));
					} else {
						voCells.add(new VOCell(new ArrayList<Tuple>(),
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
					tuples.add(leafEntries[cId].getTuple());
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
		leafEntries = new Entry[Global.TOTN];
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
			leafEntries[id] = entry;
		}
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		IO.writeInt(ds, innerEntries.size());
		for (java.util.Map.Entry<Integer, Entry> entry : innerEntries
				.entrySet()) {
			IO.writeInt(ds, entry.getKey());
			entry.getValue().write(ds);
		}
		IO.writeInt(ds, leafEntries.length);
		for (int i = 0; i < leafEntries.length; i++) {
			if (leafEntries[i] == null) continue;
			IO.writeInt(ds, i);
			leafEntries[i].write(ds);
		}
	}

	@Override
	public ArrayList<VOCell> kNN(IShape query) {
		// TODO Auto-generated method stub
		return null;
	}
}
