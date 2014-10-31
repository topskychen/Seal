/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;
import index.SearchIndex.INDEX_TYPE;

import java.math.BigInteger;

import org.junit.BeforeClass;
import org.junit.Test;

import party.TrustedRegister;
import spatialindex.Point;
import timer.Timer;
import utility.EncFun.ENC_TYPE;
import utility.Global;
import utility.Seal;
import utility.Tuple;
import utility.Utility;
import crypto.AES;
import crypto.Hasher;

/**
 * @author chenqian
 * 
 */
public class TestSeal {

	static Tuple	tuple1	= new Tuple(1, new Point(new double[] { 128 }), 0,
									null, INDEX_TYPE.BTree);
	static Tuple	tuple2	= new Tuple(2, new Point(new double[] { 64 }), 0,
									null, INDEX_TYPE.BTree);
	static Seal		seal1	= null, seal2 = null;
	static Timer	timer	= new Timer();

	@BeforeClass
	public static void init() {
		TrustedRegister.sk = AES.getSampleKey();
		TrustedRegister.specifyEncFun(ENC_TYPE.Paillier, "./data/test");
		seal1 = new Seal(tuple1, TrustedRegister.genSecretShare(tuple1));
		seal2 = new Seal(tuple2, TrustedRegister.genSecretShare(tuple2));
	}

	@Test
	public void testFold() {
		// fail("Not yet implemented");
		Seal seal3 = new Seal(seal1, seal2, false);
		assertEquals(seal3.getCnt(null), new BigInteger("2"));
		Seal seal4 = new Seal(seal3, seal2, false);
		assertEquals(seal4.getCnt(null), new BigInteger("3"));
	}

	@Test
	public void testSecretShare() {
		Seal seal3 = new Seal(seal1, seal2, false);
		assertEquals(
				seal3.getSecretShare(null),
				TrustedRegister.genSecretShare(tuple1).add(
						TrustedRegister.genSecretShare(tuple2)));
	}

	@Test
	public void testDig() {
		Tuple tuple3 = new Tuple(tuple1, tuple2, -1);
		Seal seal3 = new Seal(seal1, seal2, false);
		int[] comPre = tuple3.getComPre();
		BigInteger dig = Utility.getBI(
				Hasher.hashBytes(new Integer(comPre[comPre.length - 1])
						.toString().getBytes())).multiply(new BigInteger("2"));
		assertEquals(dig, seal3.getDig(null, Global.L - comPre.length));
	}

	@Test
	public void testLargeFolding() {
		int num = 200;
		Seal[] seals = new Seal[num];
		BigInteger ps = BigInteger.ZERO;
		for (int i = 0; i < num; i++) {
			seals[i] = new Seal(new Tuple(i, new Point(new double[] { i }), 0,
					null, INDEX_TYPE.BTree),
					TrustedRegister.genSecretShare(new Tuple(i, new Point(
							new double[] { i }), 0, null, INDEX_TYPE.BTree)));
			ps = ps.add(TrustedRegister.genSecretShare(new Tuple(i, new Point(
					new double[] { i }), 0, null, INDEX_TYPE.BTree)));
		}
		timer.reset();
		Seal sealA = seals[0];
		for (int i = 1; i < num; i++) {
			sealA = new Seal(sealA, seals[i], false);
		}
		timer.stop();
		System.out.println("Time consumes: " + timer.timeElapseinMs() +
		 " ms");
		assertEquals(new BigInteger(new Integer(num).toString()),
				sealA.getCnt(null));
		assertEquals(ps, sealA.getSecretShare(null));
	}

	@Test
	public void testBinaryTree() {
		int num = 200;
		Seal[] seals = new Seal[num];
		BigInteger ps = BigInteger.ZERO;
		for (int i = 0; i < num; i++) {
			seals[i] = new Seal(new Tuple(i, new Point(new double[] { i }), 0,
					null, INDEX_TYPE.BTree),
					TrustedRegister.genSecretShare(new Tuple(i, new Point(
							new double[] { i }), 0, null, INDEX_TYPE.BTree)));
			ps = ps.add(TrustedRegister.genSecretShare(new Tuple(i, new Point(
					new double[] { i }), 0, null, INDEX_TYPE.BTree)));
		}
		timer.reset();
		while (num > 1) {
			int newNum = 0;
			for (int i = 0; i < num; i += 2) {
				if (i + 1 == num) {
					seals[newNum++] = seals[i];
				} else {
					Seal l = seals[i];
					Seal r = seals[i + 1];
					seals[newNum++] = new Seal(l, r, false);
				}
			}
			num = newNum;
		}
		timer.stop();
		System.out.println("Time consumes: " + timer.timeElapseinMs() + " ms");
		// System.out.println(seals[0]);
		assertEquals(ps, seals[0].getSecretShare(null));
	}

}
