/**
 * 
 */
package party;

import index.Entry;
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
	 * @param p
	 */
	public void addValue(int v) {
		entries.add(new Entry(id, new Tuple(v, 0), null));
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
	
	public static void initOneDim(ArrayList<DataOwner> dataOwners, String fileName) {
		File doFile = new File(fileName + ".do");
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
				DataOutputStream ds = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(doFile)));
				ArrayList<Integer> values = loadValuesFromFile(fileName + ".pl");
				IO.writeInt(ds, values.size());
				for (int i = 0; i < values.size(); i ++) {
					DataOwner dataOwner = new DataOwner(i);
					dataOwner.addValue(values.get(i));
					dataOwner.setSecretShare(TrustedRegister.genSecretShare(i, dataOwner.getFirstEntry().getTuple()));
//					dataOwner.setSecretShare(Constants.PRIME_P);
					dataOwner.prepareSeals();
					dataOwner.write(ds);
					dataOwners.add(dataOwner);
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
		}
	}
	
	public static ArrayList<Integer> loadValuesFromFile(String file) {
		try {
			ArrayList<Integer> data = new ArrayList<>();
			Scanner in = new Scanner(new File(file));
			while(in.hasNext()) {
				data.add(Integer.parseInt(in.nextLine()));
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
				Entry e = new Entry(-1); e.read(ds);
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
