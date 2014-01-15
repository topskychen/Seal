/**
 * 
 */
package utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

import party.TrustedRegister;
import utility.EncFun.ENC_TYPE;
import crypto.Hasher;
import io.IO;
import io.RW;

/**
 * @author chenqian
 *
 */
public class Seal implements RW{

	BigInteger content = null;
	
	/**
	 * Construction based on two children
	 * @param a
	 * @param b
	 */
	public Seal(Seal a, Seal b) {
		content = fold(a.content, b.content);
	}
	
	/**
	 * Fold two seals according to different types.
	 * @param content1
	 * @param content2
	 * @return
	 */
	public BigInteger fold(BigInteger content1, BigInteger content2) {
		BigInteger ans = null;
		if (TrustedRegister.type == ENC_TYPE.Paillier) {
			ans = content1.multiply(content2).mod(EncFun.paillier.nsquare);
		} else {
			ans = content1.add(content2).mod(TrustedRegister.mod);
		}
		return ans;
	}
	
	/**
	 * Construct a seal with a tuple and a secretShare.
	 * @param tuple
	 * @param secretShare
	 */
	public Seal(Tuple tuple, byte[] secretShare) {
		content = getBI(secretShare);
		content = content.shiftLeft(24);
		content = content.add(BigInteger.ONE);
		if (tuple.getDim() == 1) {
			// Index every four bits, total 8 segments, 24 bits padding
			// 160 bits secret share, 24 bits counting
			// total = (160 + 24) * 8 + 128 + 24
			int value = tuple.getLowPoint().getCoord(0);
			for (int i = 0; i < 8; i ++) {
				content = content.shiftLeft(24 + 160);
				int v = (int) (value & ((1L << (i * 4 + 4)) - 1));
				byte[] hash = Hasher.hashBytes(new Integer(v).toString().getBytes());
				content = content.xor(getBI(hash));
			}
			// Encrypt with Paillier or one-time pad
			
		} else if (tuple.getDim() == 2) {
			
		} else {
			throw new IllegalStateException("Dim " + tuple.getDim() + " is not supported yet.");
		}
	}
	
	/**
	 * Get the corresponding bigInteger based on the byte[].
	 * @param bytes
	 * @return
	 */
	private BigInteger getBI(byte[] bytes) {
		return new BigInteger(IO.toHexFromBytes(bytes), 16);
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
		Seal seal = new Seal(tuple, new byte[]{'a'});
		System.out.println(seal.content.toString(2));
	}

	@Override
	public void read(DataInputStream ds) {
		// TODO Auto-generated method stub
		content = IO.readBigInteger(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		IO.writeBigInteger(ds, content);
	}

}
