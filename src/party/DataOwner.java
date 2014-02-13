/**
 * 
 */
package party;

import index.Entry;
import index.MemRTree;
import index.SearchIndex.INDEX_TYPE;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

import spatialindex.IShape;
import spatialindex.Point;
import utility.Constants;
import utility.Seal;
import utility.Tuple;

/**
 * @author chenqian
 *
 */
public class DataOwner {

	private int						id				= -1;
	private ArrayList<IShape>		points			= null;
	private ArrayList<Entry> 		entries 		= null;
	private ArrayList<BigInteger>	SSs			 	= null;
	
	public int getId() {
		return id;
	}
	
	/**
	 * Get the next entry from the data owner.
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
	/**
	 * Append a value to the data owner.
	 * Remember to call the function prepareSeals afterward.
	 * Pay attention, only the one-dim case is considered.
	 * @param p
	 * @param comPre
	 * @param type
	 */
	public void prepareEntry(int runId, int[] comPre, INDEX_TYPE type) {
		Tuple tuple = new Tuple(getId(), getPoint(runId), runId, comPre, type);
		Seal seal = new Seal(tuple, getSS(runId));
		Entry entry = new Entry(tuple, seal);
		entries.add(runId, entry);
	}
	
	public void clear() {
		entries.clear();
	}
	
	public DataOwner(int id, ArrayList<IShape> points) {
		this.id 		= id;
		this.points 	= points;
		this.entries 	= new ArrayList<Entry>(points.size());
		this.SSs		= new ArrayList<BigInteger>(points.size());
		for (int i = 0; i < points.size(); i ++) {
			this.SSs.add(TrustedRegister.genSecretShare(i));
			this.entries.add(null);
		} 
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public int[] comPre(MemRTree rtree, int runId) {
		ArrayList<Integer> path = new ArrayList<Integer>();
		rtree.getPath(rtree.getRootId(), getPoint(runId), id, path);
		path.add(rtree.getRootId());
		int[] comPre = new int[utility.Constants.L];
		for (int j = 0; j < utility.Constants.L; j ++) comPre[j] = id;
		for (int j = 0, k = path.size() - 1; j < utility.Constants.L && k >= 0 ; j ++, k --) {
			comPre[j] = path.get(k);
		}
		return comPre;
	}
	
	public static void prepare(ArrayList<DataOwner> dataOwners, INDEX_TYPE type, int runId) {
		MemRTree rtree = null;
		if (type == INDEX_TYPE.RTree) {
			rtree = MemRTree.createTree();
			for (DataOwner owner : dataOwners) {
				rtree.insertData(null, owner.getPoint(runId), owner.getId());
			}
			if (Constants.RT_VERBOSE) System.out.println(rtree);
		} 
		BigInteger totalSS = BigInteger.ZERO;
		for (DataOwner owner : dataOwners) {
			if (type == INDEX_TYPE.BTree || type == INDEX_TYPE.QTree) {
				owner.prepareEntry(runId, null, type);
			} else if (type == INDEX_TYPE.RTree) {
				owner.prepareEntry(runId, owner.comPre(rtree, runId), type);
			} else {
				throw new IllegalStateException("No such index!");
			}
			totalSS = totalSS.add(owner.getSS(runId));
		}
		TrustedRegister.totalSS.put(runId, totalSS);
	}
	
	public static void initData(ArrayList<DataOwner> dataOwners, String fileName, INDEX_TYPE type) {
		File file = new File(fileName + ".pl");
		if (file.exists()) {
			try {
				if (dataOwners == null) {
					dataOwners = new ArrayList<DataOwner>();
				}
				Scanner in = new Scanner(new BufferedInputStream(new FileInputStream(file)));
				while (in.hasNext()) {
					int id = Integer.parseInt(in.nextLine());
					ArrayList<IShape> points = parsePoints(in.nextLine());
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
		String[] tks = line.split(" ");
		double[] coords = new double[tks.length];
		for (int i = 0; i < coords.length; i ++) {
			coords[i] = Integer.parseInt(tks[i]);
		}
		return (IShape) new Point(coords);
	}
	
	public static ArrayList<IShape> parsePoints(String points) {
		String[] lines = points.split("\t");
		ArrayList<IShape> data = new ArrayList<IShape>();
		for (String line : lines) {
			data.add(parsePoint(line));
		}
		return data;
	}
}

