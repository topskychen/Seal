/**
 * 
 */
package index;

import io.IO;
import io.RW;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import party.TrustedRegister;
import spatialindex.IShape;
import utility.Global;
import utility.Tuple;
import utility.Utility;
import crypto.Constants;
import crypto.Hasher;

/**
 * @author chenqian
 *
 */
public class VOCell implements RW{

	Set<Integer>		ansIds		= null;
	ArrayList<Tuple>	tuples 		= null;
	ArrayList<Integer>	counts 		= null;
	int					lev			= -1;
	int 				entryNum	= 0;
	Entry 				entry		= null;
	BigInteger 			ps 			= null;
	TrustedRegister 	tr 			= TrustedRegister.getInstance();
	
	/**
	 * Get the partial secret share.
	 * @return
	 */
	public BigInteger getPartialSS() {
		return ps;
	}
	
	/**
	 * See algorithm in paper.
	 * @param query
	 * @return
	 */
	public boolean verify(IShape query) {
		BigInteger random = Constants.PRIME_P.multiply(
				new BigInteger(new Integer(entry.getNO()).toString())
			);
		entry = entry.clone();
		if (tuples.size() != 0) {
			BigInteger hashValue = BigInteger.ZERO;
			for (int i = 0; i < tuples.size(); ++i) {
				Tuple tuple = tuples.get(i);
				BigInteger cnt = new BigInteger(new Integer(counts.get(i)).toString());
				int id = tuple.getComPre()[lev];
				hashValue = hashValue.add(Utility.getBI(Hasher.hashBytes(new Integer(id).toString().getBytes())).multiply(cnt));
			}
			BigInteger dig = entry.getSeal().getDig(random, utility.Global.L - lev - 1); 
			if (!hashValue.equals(dig)) {
				return false;
			}
		}
		ps = entry.getSeal().getSecretShare(random);
		int[] comPre = entry.getTuple().getComPre();
		BigInteger cnt = entry.getSeal().getCnt(random);
		BigInteger dig = entry.getSeal().getDig(random, utility.Global.L - comPre.length);
		if (!Utility.getBI(Hasher.hashBytes(new Integer(comPre[comPre.length - 1]).toString().getBytes())).multiply(cnt).equals(dig)) {
			return false;
		}
		return true;
	}
	
	public int getAnsNo() {
		if (ansIds == null) return 0;
		return ansIds.size();
	}
	/**
	 * 
	 */
	public VOCell(Set<Integer> ansIds, ArrayList<Tuple> tuples, Entry entry) {
		// TODO Auto-generated constructor stub
		this.entryNum = 1;
		this.entry 	= entry;
		counts = new ArrayList<Integer>();
		if (tuples != null) {
			this.tuples = tuples;
			for (int i = 0; i < tuples.size(); ++i) counts.add(1);
			lev = Global.L - 1;
		}
		else {
			this.tuples = new ArrayList<Tuple>();
			this.tuples.add(entry.getTuple());
			counts.add(entry.getNO());
			lev = entry.getComPre().length - 1;
		}
		if (ansIds != null)
			this.ansIds = ansIds;
		else  this.ansIds = new HashSet<Integer>();
	}
	
	public VOCell(int id, Tuple tuple, Entry entry) {
		this.entryNum = 1;
		this.entry = entry;
		this.tuples = new ArrayList<Tuple>();
		counts = new ArrayList<Integer>();
		if (tuple != null) {
			this.tuples.add(tuple);
			counts.add(1);
			lev = Global.L - 1;
		}
		else {
			this.tuples.add(entry.getTuple());
			counts.add(entry.getNO());
			lev = entry.getComPre().length - 1;
		}
		this.ansIds = new HashSet<Integer>();
		if (id != -1) {
			ansIds.add(id);
		}
	}
	
	public void fold(VOCell vo) {
		ansIds.addAll(vo.ansIds);
		tuples.addAll(vo.tuples);
		counts.addAll(vo.counts);
		entryNum += vo.getEntryNum();
		entry = new Entry(entry, vo.entry, -1);
	}
	
	
	public int getEntryNum() {
		return entryNum;
	}

	public VOCell() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void read(DataInputStream ds) {
		// TODO Auto-generated method stub
		int size = IO.readInt(ds);
		tuples = new ArrayList<Tuple>(size);
		for (int i = 0; i < size; i ++) {
			Tuple tuple = new Tuple();
			tuple.read(ds);
			tuples.add(tuple);
		}
		entry = new Entry();
		entry.read(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		if (tuples == null) {
			IO.writeInt(ds, 0);
		} else {
			for (Tuple tuple : tuples) {
				tuple.write(ds);
			}
		}
		entry.write(ds);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (tuples != null) {
			sb.append("[ans] = " + tuples.size());
		} else {
			sb.append("[no]");
		}
		sb.append("\n");
		sb.append(entry);
		return sb.toString();
	}

	public int getLev() {
		return lev;
	}
	
}
