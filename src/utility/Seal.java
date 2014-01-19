/**
 * 
 */
package utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

import party.TrustedRegister;
import utility.EncFun.ENC_TYPE;
import crypto.Constants;
import crypto.Hasher;
import io.IO;
import io.RW;

/**
 * @author chenqian
 *
 */
public class Seal implements RW{

	BigInteger content 	= null;
	BigInteger cipher 	= null;
	
	public Seal(Seal seal) {
		cipher = seal.cipher;
	}
	
	/**
	 * Construction based on two children
	 * @param a
	 * @param b
	 */
	public Seal(Seal a, Seal b) {
		cipher = fold(a.cipher, b.cipher);
	}
	
	/**
	 * Fold two seals according to different types.
	 * @param content1
	 * @param content2
	 * @return
	 */
	public BigInteger fold(BigInteger cipher1, BigInteger cipher2) {
		BigInteger ans = null;
		if (TrustedRegister.type == ENC_TYPE.Paillier) {
			ans = cipher1.multiply(cipher2).mod(EncFun.paillier.nsquare);
		} else {
			ans = cipher1.add(cipher2).mod(TrustedRegister.mod);
		}
		return ans;
	}
	
	/**
	 * Construct a seal with a tuple and a secretShare.
	 * @param tuple
	 * @param secretShare
	 */
	public Seal(Tuple tuple, BigInteger secretShare) {
		content = secretShare;
		content = content.shiftLeft(24);
		content = content.add(BigInteger.ONE);
		if (tuple.getDim() == 1) {
			// Index every four bits, total 8 segments, 24 bits padding
			// 160 bits secret share, 24 bits counting
			// total = (160 + 24) * 8 + 128 + 24
			int value = tuple.getLowPoint().getCoord(0);
			for (int i = 0; i < 8; i ++) {
				content = content.shiftLeft(24 + 160);
				int v = (value >> (4 * i));
				byte[] hash = Hasher.hashBytes(new Integer(v).toString().getBytes());
				content = content.xor(Utility.getBI(hash));
			}
			// Encrypt with Paillier or one-time pad
			cipher = TrustedRegister.encFun.encrypt(content, Constants.PRIME_P);
			
		} else if (tuple.getDim() == 2) {
			
		} else {
			throw new IllegalStateException("Dim " + tuple.getDim() + " is not supported yet.");
		}
	}
	
	public BigInteger getSecretShare(BigInteger random) {
		if (content == null) content = TrustedRegister.encFun.decrypt(cipher, random);
		BigInteger ss = content.shiftRight((160 + 24) * 8 + 24).and(Utility.getBits1(128 + 24));
		return ss;
	}
	
	public BigInteger getCnt(BigInteger random) {
		if (content == null) content = TrustedRegister.encFun.decrypt(cipher, random);
		BigInteger cnt = content.shiftRight((160 + 24) * 8).and(Utility.getBits1(24));
		return cnt;
	}
	
	public BigInteger getDig(BigInteger random, int p) {
		if (content == null) content = TrustedRegister.encFun.decrypt(cipher, random);
		BigInteger dig = content.shiftRight((160 + 24) * p).and(Utility.getBits1(160 + 24));
		return dig;
	}
	
	/**
	 * 
	 */
	public Seal() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		int value = 101, i = 0;
//		int v = (int) (value & ((1L << (31 * 4 + 4)) - 1));
//		System.out.println(Integer.toBinaryString(value));
		Tuple tuple = new Tuple(1, 1);
		Seal seal = new Seal(tuple, null);
		System.out.println(seal.cipher.toString(2));
	}

	@Override
	public void read(DataInputStream ds) {
		// TODO Auto-generated method stub
		cipher = IO.readBigInteger(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		IO.writeBigInteger(ds, cipher);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[" + getSecretShare(null) + ", " + getCnt(null)  + "]");
		return sb.toString();
	}

}
