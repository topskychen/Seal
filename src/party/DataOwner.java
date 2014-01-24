/**
 * 
 */
package party;

import index.Entry;
import index.MemRTree;
import index.Point;
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
import java.util.Iterator;
import java.util.Scanner;

import multithread.MultiThread;
import multithread.Task;
import crypto.Constants;
import utility.Tuple;

/**
 * @author chenqian
 *
 */
public class DataOwner implements RW{

	private int					id				= -1;
	private ArrayList<Entry> 	entries 		= null;
	private Iterator<Entry> 	iter 			= null;
	private BigInteger			secretShare 	= null;
	
	public int getId() {
		return id;
	}
	
	/**
	 * Prepare Seals.
	 */
	public void prepareSeals() {
		for (int i = 0; i < entries.size(); i ++) {
			entries.get(i).prepareSeal(secretShare);
		}
	}
	
	/**
	 * Reset the iterator of the entries to the begin.
	 */
	public void reSetIterator() {
		iter = entries.iterator();
	}
	
	/**
	 * Get the first entry from the data owner.
	 * @return
	 */
	public Entry getFirstEntry() {
		if (entries == null) {
			throw new NullPointerException("entries is null");
		}
		return entries.get(0);
	}
	
	/**
	 * Get the next entry from the data owner.
	 * @return
	 */
	public Entry getNextEntry() {
		if (iter == null) {
			throw new NullPointerException("entries is null");
		}
		return iter.next();
	}
	
	/**
	 * Append a value to the data owner.
	 * Remember to call the function prepareSeals afterward.
	 * Pay attention, only the one-dim case is considered.
	 * @param p
	 * @param comPre
	 * @param type
	 */
	public void addValue(Point p, int[] comPre, INDEX_TYPE type) {
		entries.add(new Entry(new Tuple(id, p, 0, comPre, type), null));
	}
	
	public void clear() {
		entries.clear();
		iter = null;
	}
	
	public void setSecretShare(BigInteger secretShare) {
		this.secretShare = secretShare;
	}
	
	public DataOwner(int id) {
		this.id = id;
		this.entries = new ArrayList<Entry>();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static void initDim(ArrayList<DataOwner> dataOwners, String fileName, INDEX_TYPE type) {
		File doFile = new File(fileName + "." + type);
		TrustedRegister.secretShares.clear();
		TrustedRegister.totalSS = BigInteger.ZERO;
		if (doFile.exists()) {
			try {
				DataInputStream ds = new DataInputStream(new BufferedInputStream(new FileInputStream(doFile)));
				int size = IO.readInt(ds);
				for (int i = 0; i < size; i ++) {
					DataOwner dataOwner = new DataOwner(i); dataOwner.read(ds);
					dataOwners.add(dataOwner);
					TrustedRegister.addSecretShare(i, dataOwner.secretShare);
					TrustedRegister.totalSS = TrustedRegister.totalSS.add(dataOwner.secretShare);
				}
				ds.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				ArrayList<Point> values = loadPointsFromFile(fileName + ".pl");
				MemRTree rtree = null;
				if (type == INDEX_TYPE.RTree) {
					rtree = MemRTree.createTree();
					for (int i = 0; i < values.size(); i ++) {
						rtree.insertData(null, new spatialindex.Point(values.get(i).doubleCoords()), i);
					}
					System.out.println(rtree);
				}
				BuildTask[] tasks = new BuildTask[values.size()]; 
				for (int i = 0; i < values.size(); i ++) {
					if (type == INDEX_TYPE.BTree) {
						tasks[i] = new BuildTask(i, values.get(i), null, type);
					} else if (type == INDEX_TYPE.RTree) {
						ArrayList<Integer> path = new ArrayList<Integer>();
						rtree.getPath(rtree.getRootId(), new spatialindex.Point(values.get(i).doubleCoords()), i, path);
						path.add(rtree.getRootId());
						int[] comPre = new int[utility.Constants.L];
						for (int j = 0; j < utility.Constants.L; j ++) comPre[j] = i;
						for (int j = 0, k = path.size() - 1; j < utility.Constants.L && k >=0 ; j ++, k --) {
							comPre[j] = path.get(k);
						}
						tasks[i] = new BuildTask(i, values.get(i), comPre, type);
					} else if (type == INDEX_TYPE.QTree) {
						tasks[i] = new BuildTask(i, values.get(i), null, type);
					} else {
						throw new IllegalStateException("No such index!");
					}
				}
				new MultiThread(tasks, utility.Constants.THREAD_NUM, true, tasks.length / 50).run();
				DataOutputStream ds = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(doFile)));
				IO.writeInt(ds, values.size());
				for (int i = 0; i < tasks.length; i ++) {
					DataOwner owner = tasks[i].getOwner();
					dataOwners.add(owner);
					owner.write(ds);
					TrustedRegister.totalSS = TrustedRegister.totalSS.add(owner.secretShare);					
				}
				ds.close();
				System.out.println("Index is built.");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static ArrayList<Point> loadPointsFromFile(String file) {
		try {
			ArrayList<Point> data = new ArrayList<Point>();
			Scanner in = new Scanner(new File(file));
			while(in.hasNext()) {
				String[] tks = in.nextLine().split(" ");
				int[] coords = new int[tks.length];
				for (int i = 0; i < coords.length; i ++) {
					coords[i] = Integer.parseInt(tks[i]);
				}
				data.add(new Point(coords));
			}
			in.close();
			return data;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void read(DataInputStream ds) {
		// TODO Auto-generated method stub
		id = IO.readInt(ds);
		int size = IO.readInt(ds);
		if (size != 0) {
			entries = new ArrayList<Entry>();
			for (int i = 0; i < size; i ++) {
				Entry e = new Entry(); e.read(ds);
				entries.add(e);
			}
		}
		secretShare = IO.readBigInteger(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		IO.writeInt(ds, id);
		if (entries == null) IO.writeInt(ds, 0);
		else {
			IO.writeInt(ds, entries.size());
			for (Entry e : entries) {
				e.write(ds);
			}
		}
		IO.writeBigInteger(ds, secretShare);
	}
}

class BuildTask extends Task{
	DataOwner 	dataOwner 	= null;
	Point 		value 		= null;
	int[] 		comPre		= null;
	INDEX_TYPE 	type 		= null;
	
	public BuildTask(int id, Point value, int[] comPre, INDEX_TYPE type) {
		// TODO Auto-generated constructor stub
		this.dataOwner 	= new DataOwner(id);
		this.value 		= value;
		this.comPre		= comPre;
		this.type		= type;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		dataOwner.addValue(value, comPre, type);
		dataOwner.setSecretShare(TrustedRegister.genSecretShare(dataOwner.getFirstEntry().getTuple()));
//		dataOwner.setSecretShare(Constants.PRIME_P);
		dataOwner.prepareSeals();
//		dataOwner.write(ds);
//		dataOwners.add(dataOwner);
	} 
	
	public DataOwner getOwner() {
		return dataOwner;
	}
}
