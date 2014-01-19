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

	ArrayList<Integer> 	ids 		= null;
	ArrayList<Tuple>	tuples 		= null;
	Entry 				entry		= null;
	BigInteger 			ps 			= null;
	
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
		BigInteger random = Constants.PRIME_Q.multiply(
				new BigInteger(new Integer(entry.getNO()).toString())
			);
		if (query.inRange(entry.getLB(), entry.getHB())) {
			ps = BigInteger.ZERO;
			for (int i = 0; i < ids.size(); i ++) {
				BigInteger secretShare = TrustedRegister.genSecretShare(ids.get(i), tuples.get(i));
				ps = ps.add(secretShare);
			}
			BigInteger rs = entry.getSeal().getSecretShare(random);
			if (!ps.equals(rs)) return false;
		} else {
			ps = entry.getSeal().getSecretShare(random);
		}
		int[] comPre = entry.getComPre();
//		Utility.pi22(comPre[0]);
		BigInteger cnt = entry.getSeal().getCnt(random);
		BigInteger dig = entry.getSeal().getDig(random, comPre[1]);
		if (!Utility.getBI(Hasher.hashBytes(new Integer(comPre[0]).toString().getBytes())).multiply(cnt).equals(dig)) {
			return false;
		}
		return true;
	}
	/**
	 * 
	 */
	public VOCell(ArrayList<Integer> ids, ArrayList<Tuple> tuples, Entry entry) {
		// TODO Auto-generated constructor stub
		this.ids 	= ids;
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
		ids = new ArrayList<>(size);
		for (int i = 0; i < size; i ++) {
			ids.add(IO.readInt(ds));
		}
		tuples = new ArrayList<>(size);
		for (int i = 0; i < size; i ++) {
			Tuple tuple = new Tuple();
			tuple.read(ds);
			tuples.add(tuple);
		}
		entry = new Entry(-1);
		entry.read(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		if (ids == null) {
			IO.writeInt(ds, 0);
		} else {
			IO.writeInt(ds, ids.size());
			for (int i = 0; i < ids.size(); i ++) {
				IO.writeInt(ds, ids.get(i));
			}
			for (Tuple tuple : tuples) {
				tuple.write(ds);
			}
		}
		entry.write(ds);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(entry);
		return sb.toString();
	}
}
