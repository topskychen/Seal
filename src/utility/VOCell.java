/**
 * 
 */
package utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;

import crypto.Constants;
import crypto.Hasher;
import party.TrustedRegister;
import index.Entry;
import io.IO;
import io.RW;

/**
 * @author chenqian
 *
 */
public class VOCell implements RW{

	ArrayList<Tuple>	tuples 		= null;
	Entry 				entry		= null;
	BigInteger 			ps 			= null;
	int					ansNo		= -1;
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
	public boolean verify(Query query) {
		//TODO
		entry.getSeal().setContent(null);
		BigInteger random = Constants.PRIME_P.multiply(
				new BigInteger(new Integer(entry.getNO()).toString())
			);
		if (query.inRange(entry.getLB(), entry.getHB())) {
			ps = BigInteger.ZERO;
			for (int i = 0; i < tuples.size(); i ++) {
				BigInteger secretShare = TrustedRegister.genSecretShare(tuples.get(i));
				ps = ps.add(secretShare);
			}
			BigInteger rs = entry.getSeal().getSecretShare(random);
			if (!ps.equals(rs)) { 
//				System.out.println("x");
				return false;
			}
			ansNo = tuples.size();
		} else {
			ps = entry.getSeal().getSecretShare(random);
			ansNo = 0;
		}
		int[] comPre = entry.getTuple().getComPre();
//		Utility.pi22(comPre[0]);
		BigInteger cnt = entry.getSeal().getCnt(random);
		BigInteger dig = entry.getSeal().getDig(random, utility.Constants.L - comPre.length);
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
