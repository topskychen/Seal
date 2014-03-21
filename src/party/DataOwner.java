/**
 * 
 */
package party;

import index.Entry;
import index.MemRTree;
import index.SearchIndex.INDEX_TYPE;
import io.IO;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import spatialindex.IShape;
import spatialindex.Point;
import utility.Global;
import utility.Global.MODE;
import utility.Seal;
import utility.Tuple;

/**
 * @author chenqian
 * 
 */
public class DataOwner {

	private int								id		= -1;
	private HashMap<Integer, IShape>		points	= null;
	private HashMap<Integer, Entry>			entries	= null;
	private HashMap<Integer, BigInteger>	SSs		= null;

	public int getId() {
		return id;
	}

	/**
	 * Get the next entry from the data owner.
	 * 
	 * @return
	 */
	public Entry getEntry(int i) {
		return entries.get(i);
	}

	public IShape getPoint(int i) {
		return points.get(i);
	}

	public BigInteger getSS(int i) {
		return SSs.get(i);
	}

	public void putSS(int i, BigInteger ss) {
		SSs.put(i, ss);
	}

	/**
	 * Append a value to the data owner. Remember to call the function
	 * prepareSeals afterward. Pay attention, only the one-dim case is
	 * considered.
	 * 
	 * @param runId
	 * @param comPre
	 * @param type
	 */
	public void prepareEntry(int runId, int[] comPre, INDEX_TYPE type) {
		if (type == INDEX_TYPE.RTree && Global.G_MODE == MODE.UPDATE) {
			entries.put(runId, new Entry(new Tuple(getId(), getPoint(runId),
					runId, null, type), null));
		} else {
			Tuple tuple = new Tuple(getId(), getPoint(runId), runId, comPre,
					type);
			Seal seal = new Seal(tuple, getSS(runId));
			Entry entry = new Entry(tuple, seal);
			entries.put(runId, entry);
		}
	}

	public void clear() {
		entries.clear();
	}

	public DataOwner(int id, HashMap<Integer, IShape> points) {
		this.id = id;
		this.points = points;
		this.entries = new HashMap<Integer, Entry>();
		this.SSs = new HashMap<Integer, BigInteger>();
		for (Map.Entry<Integer, IShape> point : points.entrySet()) {
			this.SSs.put(point.getKey(),
					TrustedRegister.genSecretShare(point.getKey()));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static int[] comPre(MemRTree rtree, IShape point, int id) {
		ArrayList<Integer> path = new ArrayList<Integer>();
		rtree.getPath(rtree.getRootId(), point, id, path);
		path.add(rtree.getRootId());
		int[] comPre = new int[utility.Global.L];
		for (int j = 0; j < utility.Global.L; j++)
			comPre[j] = id;
		for (int j = 0, k = path.size() - 1; j < utility.Global.L && k >= 0; j++, k--) {
			comPre[j] = path.get(k);
		}
		return comPre;
	}

	public static void prepare(ArrayList<DataOwner> dataOwners,
			INDEX_TYPE type, int runId) {
		MemRTree rtree = null;
		if (type == INDEX_TYPE.RTree) {
			if (Global.G_MODE == MODE.REBUILD) {
				rtree = MemRTree.createTree();
				for (DataOwner owner : dataOwners) {
					if (owner.getPoint(runId) == null)
						continue;
					rtree.insertData(null, owner.getPoint(runId), owner.getId());
				}
				if (Global.RT_VERBOSE)
					System.out.println(rtree);
			}
		}
		BigInteger totalSS = BigInteger.ZERO;
		for (DataOwner owner : dataOwners) {
			if (owner.getPoint(runId) == null) {
				owner.putSS(runId, owner.getSS(runId - 1));
				if (Global.G_MODE == MODE.REBUILD)
					continue;
			} else {
				if (Global.DO_COST)
					Global.G_TIMER.reset();
				if (type == INDEX_TYPE.BTree || type == INDEX_TYPE.QTree) {
					owner.prepareEntry(runId, null, type);
				} else if (type == INDEX_TYPE.RTree) {
					if (Global.G_MODE == MODE.REBUILD) {
						owner.prepareEntry(runId, DataOwner.comPre(rtree,
								owner.getPoint(runId), owner.getId()), type);
					} else {
						owner.prepareEntry(runId, null, type);
					}
				} else {
					throw new IllegalStateException("No such index!");
				}
				if (Global.DO_COST) {
					Global.G_TIMER.stop();
					Global.STAT_DO.append(Global.G_TIMER.timeElapseinUs(),
							IO.toBytes(owner.getEntry(runId)).length);
				}
			}
			totalSS = totalSS.add(owner.getSS(runId));
		}
		TrustedRegister.totalSS.put(runId, totalSS);
	}

	public static void initData(ArrayList<DataOwner> dataOwners,
			String fileName, INDEX_TYPE type, int startTime, int runTimes) {
		HashMap<Integer, HashSet<Integer>> updateIds = null;
		if (Global.G_MODE != MODE.REBUILD) {
			updateIds = collectUpdateIds(fileName, startTime);
		}
		File file = new File(fileName + ".pl");
		if (file.exists()) {
			try {
				if (dataOwners == null) {
					dataOwners = new ArrayList<DataOwner>();
				}
				Scanner in = new Scanner(new BufferedInputStream(
						new FileInputStream(file)));
				while (in.hasNext()) {
					int id = Integer.parseInt(in.nextLine());
					HashMap<Integer, IShape> points = parsePoints(id,
							in.nextLine(), startTime, runTimes, updateIds);
					dataOwners.add(new DataOwner(id, points));
				}
				in.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("File " + file + ".pl is not existed!");
		}
	}

	public static IShape parsePoint(String line) {
		if (line.equals("")) {
			System.out.println("err!");
			return null;
		}
		String[] tks = line.split(" ");
		double[] coords = new double[tks.length];
		for (int i = 0; i < coords.length; i++) {
			coords[i] = Integer.parseInt(tks[i]);
		}
		return new Point(coords);
	}

	public static HashMap<Integer, IShape> parsePoints(int id, String points,
			int startTime, int runTimes,
			HashMap<Integer, HashSet<Integer>> updateIds) {
		String[] lines = points.split("\t");
		HashMap<Integer, IShape> data = new HashMap<Integer, IShape>();

		int lineNo = 0;
		for (String line : lines) {
			String[] tks = line.split(" ");
			int timeStamp = lineNo;
			if (timeStamp >= startTime
					&& timeStamp < startTime + runTimes
					&& (updateIds == null
							|| updateIds.containsKey(timeStamp) == false || updateIds
							.get(timeStamp).contains(id))) {
				double[] coords = new double[tks.length - 1];
				for (int i = 0; i < coords.length; i++) {
					coords[i] = Integer.parseInt(tks[i]);
				}
				data.put(timeStamp, new Point(coords));
			}
			if (timeStamp >= startTime + runTimes)
				break;
			lineNo++;
		}
		return data;
	}

	public static HashMap<Integer, HashSet<Integer>> collectUpdateIds(
			String fileName, int startTime) {
		HashMap<Integer, HashSet<Integer>> updateIds = new HashMap<Integer, HashSet<Integer>>();
		Scanner in = null;
		try {
			in = new Scanner(
					new File(fileName + "_" + Global.UPDATE_RT + ".ur"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int lineNo = 0;
		HashSet<Integer> ids = new HashSet<Integer>();
		for (int i = 0; i < Global.TOTN; i++) {
			ids.add(i);
		}
		updateIds.put(startTime, ids);
		while (in.hasNext()) {
			String[] tks = in.nextLine().split(" ");
			ids = new HashSet<Integer>();
			for (String tk : tks) {
				if (tk.equals(""))
					break;
				ids.add(Integer.parseInt(tk));
			}
			updateIds.put(startTime + (++lineNo), ids);
		}
		in.close();
		return updateIds;
	}
}
