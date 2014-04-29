/**
 * 
 */
package utility;

import index.Entry;
import index.SearchIndex.INDEX_TYPE;
import io.IO;
import io.RW;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;

import party.TrustedRegister;
import spatialindex.Point;
import timer.Timer;
import utility.EncFun.ENC_TYPE;
import crypto.AES;
import crypto.Constants;
import crypto.Hasher;

/**
 * @author chenqian
 * 
 */
public class Seal implements RW {

	// BigInteger content = null;
	BigInteger	cipher	= null;

	public Seal(Seal seal) {
		cipher = seal.cipher;
	}

	public Seal(Seal[] seals) {
		// TODO
		cipher = seals[0].cipher;
		for (int i = 1; i < seals.length; i++) {
			cipher = fold(cipher, seals[i].cipher);
		}
	}

	/**
	 * Construction based on two children The reverse indicate whether reverse
	 * the b or not
	 * 
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
			ans = cipher1.multiply(cipher2.modInverse(EncFun.paillier.nsquare))
					.mod(EncFun.paillier.nsquare);
		} else {
			ans = cipher1.subtract(cipher2).add(TrustedRegister.mod)
					.mod(TrustedRegister.mod);
		}
		return ans;
	}

	/**
	 * Fold two seals according to different types.
	 * 
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
	 * 
	 * @param tuple
	 * @param secretShare
	 */
	public Seal(Tuple tuple, BigInteger secretShare) {
		BigInteger content = secretShare;
		content = content.shiftLeft(24);
		content = content.add(BigInteger.ONE);
		// int value = tuple.getLowPoint().getCoord(0);
		int[] comPre = tuple.getComPre();
		// for (int i = 0; i < utility.Constants.L - Math.min(comPre.length,
		// utility.Constants.L); i ++) { // shift
		// content = content.shiftLeft(24 + 160);
		// }
		for (int i = 0; i < utility.Global.L; i++) {
			content = content.shiftLeft(24 + 160);
			// int v = (value >> (4 * i));
			byte[] hash = Hasher.hashBytes(new Integer(comPre[i]).toString()
					.getBytes());
			content = content.xor(Utility.getBI(hash));
		}
		// Encrypt with Paillier or one-time pad
		cipher = TrustedRegister.encFun.encrypt(content, Constants.PRIME_P);
		if (utility.Global.DO_COST) {

		}
	}

	public BigInteger getSecretShare(BigInteger random) {
		// if (content == null)
		BigInteger content = TrustedRegister.encFun.decrypt(cipher, random);
		BigInteger ss = content.shiftRight((160 + 24) * utility.Global.L + 24)
				.and(utility.Global.BITS152);
		return ss;
	}

	public BigInteger getCnt(BigInteger random) {
		// if (content == null)
		BigInteger content = TrustedRegister.encFun.decrypt(cipher, random);
		BigInteger cnt = content.shiftRight((160 + 24) * utility.Global.L).and(
				utility.Global.BITS24);
		return cnt;
	}

	public BigInteger getDig(BigInteger random, int p) {
		// if (content == null)
		BigInteger content = TrustedRegister.encFun.decrypt(cipher, random);
		BigInteger dig = content.shiftRight((160 + 24) * p).and(
				utility.Global.BITS184);
		return dig;
	}

	public static Entry getSample(int id) {
		Tuple tuple = new Tuple(id, new Point(new double[] { id, id }), id,
				new int[] { 1, 2, 3, 4, 5, 6 }, INDEX_TYPE.RTree);
		Seal seal = new Seal(tuple, TrustedRegister.genSecretShare(tuple));
		return new Entry(tuple, seal);
	}

	/**
	 * one-time pad 39.7 us
	 * Paillier 104004 us
	 * @return
	 */
	public ArrayList<Entry> testGenTime() {
		TrustedRegister.sk = AES.getSampleKey();
		TrustedRegister.specifyEncFun(ENC_TYPE.OTPad, Sim.fileName);
		Timer timer = new Timer();
		timer.reset();
		int times = 1000;
		ArrayList<Entry> entries = new ArrayList<Entry>();
		Seal[] seals = new Seal[times];
		for (int i = 0; i < times; i++) {
			Tuple tuple = new Tuple(i, new Point(new double[] { i, i }), i,
					new int[] { 1, 2, 3, 4, 5, 6 }, INDEX_TYPE.RTree);
			Seal seal = new Seal(tuple, TrustedRegister.genSecretShare(tuple));
			seals[i] = seal;
			entries.add(new Entry(tuple, seal));
		}
		timer.stop();
		System.out.println("Time consumes @ gen: " + timer.timeElapseinUs()
				/ times + " us");

		return entries;
	}

	/**
	 * one-time pad: 0.922 us
	 * 
	 */
	public void testFolding() {
		TrustedRegister.sk = AES.getSampleKey();
		TrustedRegister.specifyEncFun(ENC_TYPE.OTPad, Sim.fileName);
		Timer timer = new Timer();
		int times = 1000000;
		Seal[] seals = new Seal[times];
		Tuple tuple = new Tuple(0, new Point(new double[] { 0, 0 }), 0,
				new int[] { 1, 2, 3, 4, 5, 6 }, INDEX_TYPE.RTree);
		Seal seal = new Seal(tuple, TrustedRegister.genSecretShare(tuple));
		timer.reset();
		for (int i = 0; i < times; i++) {
			seals[i] = seal;
		}
		Seal fseal = new Seal(seals);
		timer.stop();
		System.out.println(fseal.hashCode());
		System.out.println("Time consumes @ fold: " + timer.timeElapseinUs()
				 + " us");
	}

	/**
	 * one-time pad 28.6 us
	 * Paillier 118641.34
	 */
	public void testVrfTime(ArrayList<Entry> entries) {
		TrustedRegister.sk = AES.getSampleKey();
		TrustedRegister.specifyEncFun(ENC_TYPE.OTPad, Sim.fileName);
		Timer timer = new Timer();
		timer.reset();
		int times = 1000;
		for (int i = 0; i < times; i++) {
			Entry entry = entries.get(i);
			entry.verify();
		}
		timer.stop();
		System.out.println("Time consumes @ vrf: " + timer.timeElapseinUs()
				/ times + " us");
	}

	/**
	 * 
	 */
	public Seal() {
	}

	public Seal(BigInteger cipher) {
		this.cipher = cipher;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Seal seal = new Seal();
		seal.testFolding();
		ArrayList<Entry> entries = seal.testGenTime();
		seal.testVrfTime(entries);
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
		// sb.append("[" + + ", " + + "]");
		return sb.toString();
	}

	public Seal clone() {
		return new Seal(cipher);
	}

}
