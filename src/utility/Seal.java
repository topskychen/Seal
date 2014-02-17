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
	
	public Seal(Seal[] seals) {
		//TODO
		cipher = seals[0].cipher;
		for (int i = 1; i < seals.length; i ++) {
			cipher = fold(cipher, seals[i].cipher);
		}
	}
	
	/**
	 * Construction based on two children
	 * The reverse indicate whether reverse the b or not
	 * @param a
	 * @param b
	 * @param reserse
	 */
	public Seal(Seal a, Seal b, boolean reverse) {
		if (reverse) {
			cipher = reverseFold(a.cipher, b.cipher);
		} else {
			cipher = fold(a.cipher, b.cipher);			
		}
	}
	
	private BigInteger reverseFold(BigInteger cipher1, BigInteger cipher2) {
		// TODO Auto-generated method stub
		BigInteger ans = null;
		if (TrustedRegister.type == ENC_TYPE.Paillier) {
			ans = cipher1.multiply(cipher2.modInverse(EncFun.paillier.nsquare)).mod(EncFun.paillier.nsquare);
		} else {
			ans = cipher1.subtract(cipher2).add(TrustedRegister.mod).mod(TrustedRegister.mod);
		}
		return ans;
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
//		int value = tuple.getLowPoint().getCoord(0);
		int[] comPre = tuple.getComPre();
		for (int i = 0; i < comPre.length; i ++) {
			content = content.shiftLeft(24 + 160);
//			int v = (value >> (4 * i));
			byte[] hash = Hasher.hashBytes(new Integer(comPre[i]).toString().getBytes());
			content = content.xor(Utility.getBI(hash));
		}
		// Encrypt with Paillier or one-time pad
		cipher = TrustedRegister.encFun.encrypt(content, Constants.PRIME_P);
	}
	
	public BigInteger getSecretShare(BigInteger random) {
		if (content == null) content = TrustedRegister.encFun.decrypt(cipher, random);
		BigInteger ss = content.shiftRight((160 + 24) * utility.Constants.L + 24).and(utility.Constants.BITS152);
		return ss;
	}
	
	public BigInteger getCnt(BigInteger random) {
		if (content == null) content = TrustedRegister.encFun.decrypt(cipher, random);
		BigInteger cnt = content.shiftRight((160 + 24) * utility.Constants.L).and(utility.Constants.BITS24);
		return cnt;
	}
	
	public BigInteger getDig(BigInteger random, int p) {
		if (content == null) content = TrustedRegister.encFun.decrypt(cipher, random);
		BigInteger dig = content.shiftRight((160 + 24) * p).and(utility.Constants.BITS184);
		return dig;
	}
	
	/**
	 * 
	 */
	public Seal() {
		// TODO Auto-generated constructor stub
	}
	
	public Seal(BigInteger cipher) {
		this.cipher = cipher;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		int value = 101, i = 0;
//		int v = (int) (value & ((1L << (31 * 4 + 4)) - 1));
//		System.out.println(Integer.toBinaryString(value));
		
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
	
	public BigInteger getContent() {
		return content;
	}

	public void setContent(BigInteger content) {
		// TODO Auto-generated method stub
		this.content = content;
	} 
	
	public Seal clone() {
		return new Seal(cipher);
	}

}
