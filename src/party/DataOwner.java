/**
 * 
 */
package party;

import index.Entry;
import index.MemQTree;
import index.MemRTree;
import index.SearchIndex;
import index.SearchIndex.INDEX_TYPE;
import io.IO;
import io.RW;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import multithread.MultiThread;
import spatialindex.IShape;
import spatialindex.Point;
import utility.Global;
import utility.Simulator;

/**
 * @author chenqian
 * 
 */
public class DataOwner implements Runnable, RW {

	private int					id			= -1;
	private List<Entry>			entries		= null;
	private List<BigInteger>	SSs			= null;
	private Set<Integer>		updateTS	= null;
	private boolean				needUpdate	= false;	// remember to set to
														// false
	private List<IShape>		points		= null;

	public void addTS(int ts) {
		updateTS.add(ts);
	}

	public Point getPoint(int i) {
		return (Point) points.get(i);
	}

	public int getId() {
		return id;
	}

	// public Entry getEntry(int i) {
	// return entries.get(i);
	// }

	public void addEntry(Entry entry) {
		entries.add(entry);
	}

	// public Seal getSeal(int i) {
	// return getEntry(i).getSeal();
	// }

	// public BigInteger getSS(int i) {
	// return SSs.get(i);
	// }

	public void addSS(BigInteger ss) {
		SSs.add(ss);
	}

	// /**
	// * Append a value to the data owner. Remember to call the function
	// * prepareSeals afterward. Pay attention, only the one-dim case is
	// * considered.
	// *
	// * @param runId
	// * @param comPre
	// * @param type
	// */
	// public void prepareEntry(int runId, int[] comPre, INDEX_TYPE type) {
	// if (type == INDEX_TYPE.RTree
	// && (Global.G_MODE == MODE.UPDATE || Global.G_MODE == MODE.LOOSE)) {
	// entries.put(runId, new Entry(new Tuple(getId(), getPoint(runId),
	// runId, null, type), null));
	// } else {
	// Tuple tuple = new Tuple(getId(), getPoint(runId), runId, comPre,
	// type);
	// Seal seal = new Seal(tuple, getSS(runId));
	// Entry entry = new Entry(tuple, seal);
	// entries.set(runId, entry);
	// }
	// }

	public DataOwner() {
	}

	public DataOwner(int id, List<IShape> points) {
		this.id = id;
		this.points = points;
		entries = new LinkedList<Entry>();
		SSs = new LinkedList<BigInteger>();
		updateTS = new HashSet<Integer>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	// public static void prepare(ArrayList<DataOwner> dataOwners,
	// INDEX_TYPE type, int runId) {
	// MemRTree rtree = null;
	// if (type == INDEX_TYPE.RTree) {
	// if (Global.G_MODE == MODE.REBUILD) {
	// rtree = MemRTree.createTree(Global.G_Dim);
	// for (DataOwner owner : dataOwners) {
	// if (owner.getPoint(runId) == null)
	// continue;
	// rtree.insertData(null, owner.getPoint(runId), owner.getId());
	// }
	// if (Global.RT_VERBOSE)
	// System.out.println(rtree);
	// }
	// }
	// BigInteger totalSS = BigInteger.ZERO;
	// for (DataOwner owner : dataOwners) {
	// if (owner.getPoint(runId) == null) {
	// owner.putSS(runId, owner.getSS(runId - 1));
	// if (Global.G_MODE == MODE.REBUILD)
	// continue;
	// } else {
	// if (Global.DO_COST)
	// Global.G_TIMER.reset();
	// if (type == INDEX_TYPE.BTree || type == INDEX_TYPE.QTree) {
	// owner.prepareEntry(runId, null, type);
	// } else if (type == INDEX_TYPE.RTree) {
	// if (Global.G_MODE == MODE.REBUILD) {
	// owner.prepareEntry(runId, DataOwner.comPre(rtree,
	// owner.getPoint(runId), owner.getId()), type);
	// } else {
	// owner.prepareEntry(runId, null, type);
	// }
	// } else {
	// throw new IllegalStateException("No such index!");
	// }
	// if (Global.DO_COST) {
	// Global.TIMER.stop();
	// Global.STAT_DO.append(Global.TIMER.timeElapseinUs(),
	// IO.toBytes(owner.getEntry(runId)).length);
	// }
	// }
	// totalSS = totalSS.add(owner.getSS(runId));
	// }
	// ts.totalSS.put(runId, totalSS);
	// }

	/**
	 * Load the DOs from file
	 * 
	 * @param file
	 * @param doNO
	 */
	public static void loadDOsFromFile(List<DataOwner> owners, File file,
			int doNO) {
		DataInputStream ds;
		try {
			ds = new DataInputStream(new BufferedInputStream(
					new FileInputStream(file)));
			for (int i = 0; i < doNO; ++i) {
				DataOwner owner = new DataOwner();
				owner.read(ds);
				owners.add(owner);
			}
			ds.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveDOstoFile(List<DataOwner> owners, File file, int doNO) {
		DataOutputStream ds;
		try {
			ds = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(file)));
			for (DataOwner owner : owners) {
				owner.write(ds);
			}
			ds.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void computeSeals(List<DataOwner> owners, INDEX_TYPE type,
			Simulator sim) {
		SearchIndex index = null;
		if (type == INDEX_TYPE.QTree) {
			index = MemQTree.createTree(sim);
		} else if (type == INDEX_TYPE.RTree) {
			index = MemRTree.createTree(sim);
			for (DataOwner owner : owners) {
				((MemRTree) index)
						.insertData(null, owner.getPoint(0), owner.id);
			}
		} else {
			System.out.println("No this type of index is supported");
			return;
		}
		BigInteger secret = BigInteger.ZERO;
		for (DataOwner owner : owners) {
			secret = owner.update(0, secret, sim.getTrustedRegister(), new Entry(owner.getId(), owner.getPoint(0), 0,
					index));
		}
		sim.getTrustedRegister().putTotalSecret(0, secret);
		new MultiThread(owners.toArray(new Runnable[0]), Global.THREAD_NUM,
				true, 50).run();
	}

	public static void initData(List<DataOwner> owners, Simulator sim) {
		File file = new File(sim.getFileName() + "_" + sim.getDim() + "_"
				+ sim.getTotalN() + "." + sim.getIndexType());
		if (file.exists()) {
			loadDOsFromFile(owners, file, sim.getTotalN());
			loadUpdatesFromFile(
					owners,
					sim.getFileName() + "_" + sim.getUpdateRate() + "_"
							+ sim.getTotalN() + ".up");
			BigInteger secret = BigInteger.ZERO;
			for (DataOwner owner : owners) {
				secret = owner.getLastSS();
			}
			sim.getTrustedRegister().putTotalSecret(0, secret);
		} else {
			loadPointsFromFile(owners, sim.getFileName() + "_" + sim.getDim()
					+ "_" + sim.getTotalN() + ".pl");
			loadUpdatesFromFile(owners, sim.getFileName() + "_" + sim.getUpdateRate()
					+ "_" + sim.getTotalN() + ".up");
			computeSeals(owners, sim.getIndexType(), sim);
			saveDOstoFile(owners, file, sim.getTotalN());
		}
	}

	private static void loadPointsFromFile(List<DataOwner> owners,
			String fileName) {
		File plainFile = new File(fileName);
		try {
			Scanner in = new Scanner(new BufferedInputStream(
					new FileInputStream(plainFile)));
			int id = 0;
			while (in.hasNext()) {
				List<IShape> points = parsePoints(in.nextLine());
				owners.add(new DataOwner(id++, points));
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static List<IShape> parsePoints(String points) {
		String[] lines = points.split("\t");
		List<IShape> data = new LinkedList<IShape>();

		for (String line : lines) {
			String[] tks = line.split(" ");
			double[] coords = new double[tks.length];
			for (int i = 0; i < coords.length; i++) {
				coords[i] = Double.parseDouble(tks[i]);
			}
			data.add(new Point(coords));
		}
		return data;
	}

	/**
	 * Collect update ids
	 * 
	 * @param fileName
	 * @return
	 */
	public static void loadUpdatesFromFile(List<DataOwner> owners,
			String fileName) {
		Map<Integer, DataOwner> map = new HashMap<>();
		int id = 0;
		for (DataOwner owner : owners) {
			map.put(id++, owner);
		}
		Scanner in = null;
		try {
			in = new Scanner(new File(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/**
		 * ts begins with 1
		 */
		int ts = 1;
		while (in.hasNext()) {
			String[] tks = in.nextLine().trim().split(" ");
			for (String tk : tks) {
				id = Integer.parseInt(tk);
				map.get(id).addTS(ts);
			}
			ts++;
		}
		in.close();
	}

	@Override
	public void run() {
		if (needUpdate) {
			Entry entry = getLastEntry();
			entry.prepareSeal(getLastSS());
		}
		needUpdate = false;
	}

	@Override
	public void read(DataInputStream ds) {
		id = IO.readInt(ds);
		int size = IO.readInt(ds);
		entries = new LinkedList<Entry>();
		for (int i = 0; i < size; ++i) {
			Entry entry = new Entry();
			entry.read(ds);
			entries.add(entry);
		}
		SSs = new LinkedList<BigInteger>();
		size = IO.readInt(ds);
		for (int i = 0; i < size; ++i) {
			SSs.add(IO.readBigInteger(ds));
		}
		size = IO.readInt(ds);
		updateTS = new HashSet<Integer>();
		for (int i = 0; i < size; ++i) {
			updateTS.add(IO.readInt(ds));
		}
		size = IO.readInt(ds);
		points = new ArrayList<IShape>();
		for (int i = 0; i < size; ++i) {
			points.add(new Point(IO.readDoubleArray(ds)));
		}
	}

	@Override
	public void write(DataOutputStream ds) {
		IO.writeInt(ds, id);
		int size = entries.size();
		IO.writeInt(ds, size);
		for (Entry entry : entries) {
			entry.write(ds);
		}
		IO.writeInt(ds, SSs.size());
		for (BigInteger entry : SSs) {
			IO.writeBigInteger(ds, entry);
		}
		IO.writeInt(ds, updateTS.size());
		for (int ts : updateTS) {
			IO.writeInt(ds, ts);
		}
		IO.writeInt(ds, points.size());
		for (IShape point : points) {
			IO.writeDoubleArray(ds, ((Point) point).m_pCoords);
		}
	}

	private BigInteger update(int runId, BigInteger secret, TrustedRegister ts, Entry entry) {
		needUpdate = true;
		if (getLastSS() != null)
			secret = secret.subtract(getLastSS());
		addSS(ts.genSecretShare(runId));
		addEntry(entry);
		secret = secret.add(getLastSS());
		return secret;
	}

	private BigInteger getLastSS() {
		if (SSs.isEmpty())
			return null;
		return ((LinkedList<BigInteger>) SSs).getLast();
	}

	public static void update(List<DataOwner> owners, int runId,
			Simulator sim) {
		
		ArrayList<Entry> entries = new ArrayList<Entry>();
		
		/**
		 * Prepare total secret and entries
		 */
		if (runId != 0) {
			//TODO if rtree...
			BigInteger secret = sim.getTrustedRegister().getTotalSS(runId - 1);
			for (DataOwner owner : owners) {
				if (owner.needUpdate(runId)) {
					secret = owner.update(runId, secret, sim.getTrustedRegister(), new Entry(owner.getId(), owner.getPoint(runId), runId, sim.getSP().index));
					entries.add(owner.getLastEntry());
				}
			}
			sim.getTrustedRegister().putTotalSecret(runId, secret);
			
			/**
			 * Prepare seals
			 */
			new MultiThread(owners.toArray(new Runnable[0]), Global.THREAD_NUM, true, 50).run();
		} else {
			for (DataOwner owner : owners) {
				entries.add(owner.getLastEntry());
			} 
		}
				
		
		/**
		 * Update the index
		 */
		sim.getSP().updateIndex(entries);
		
	}

	private Entry getLastEntry() {
		if (entries.isEmpty()) return null;
		return ((LinkedList<Entry>) entries).getLast();
	}

	private boolean needUpdate(int runId) {
		if (updateTS.contains(runId))
			return true;
		return false;
	}

}
