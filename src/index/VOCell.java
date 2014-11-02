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
import java.util.TreeSet;

import party.TrustedRegister;
import spatialindex.IShape;
import utility.Tuple;
import utility.Utility;
import crypto.Constants;
import crypto.Hasher;

/**
 * @author chenqian
 *
 */
public class VOCell implements RW{

	ArrayList<Tuple>	tuples 		= null;
	Entry 				entry		= null;
	BigInteger 			ps 			= null;
	int					ansNo		= 0;
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
	public boolean verify(IShape query, TreeSet<Integer> ansIds) {
		BigInteger random = Constants.PRIME_P.multiply(
				new BigInteger(new Integer(entry.getNO()).toString())
			);
		entry = entry.clone();
		if (ansNo != 0) {
			ps = BigInteger.ZERO;
			for (Tuple tuple : tuples) {
				BigInteger secretShare = tr.genSecretShare(tuple.getTS());
				ps = ps.add(secretShare);
				ansIds.add(tuple.getId());
			}
			BigInteger rs = entry.getSeal().getSecretShare(random);
			if (!ps.equals(rs)) { 
				return false;
			}
		} else {
			ps = entry.getSeal().getSecretShare(random);
		}
		int[] comPre = entry.getTuple().getComPre();
		BigInteger cnt = entry.getSeal().getCnt(random);
		BigInteger dig = entry.getSeal().getDig(random, utility.Global.L - comPre.length);
		if (!Utility.getBI(Hasher.hashBytes(new Integer(comPre[comPre.length - 1]).toString().getBytes())).multiply(cnt).equals(dig)) {
			return false;
		}
		return true;
	}
	
	public int getAnsNo() {
		return ansNo;
	}
	/**
	 * 
	 */
	public VOCell(ArrayList<Tuple> tuples, Entry entry) {
		// TODO Auto-generated constructor stub
		this.entry 	= entry;
		this.tuples = tuples;
		if (tuples != null)
			this.ansNo = tuples.size();
	}
	
	public VOCell(Tuple tuple, Entry entry) {
		this.entry = entry;
		this.tuples = new ArrayList<Tuple>();
		this.tuples.add(tuple);
		if (tuples != null) {
			this.ansNo = tuples.size();
		}
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
}
