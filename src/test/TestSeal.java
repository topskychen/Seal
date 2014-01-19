/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import crypto.Hasher;
import party.TrustedRegister;
import timer.Timer;
import utility.EncFun.ENC_TYPE;
import utility.Seal;
import utility.Tuple;
import utility.Utility;

/**
 * @author chenqian
 *
 */
public class TestSeal {

	static Seal seal1 = null, seal2 = null;
	static Timer timer = new Timer();
	
	@BeforeClass
	public static void init() {
		TrustedRegister.specifyEncFun(ENC_TYPE.Paillier, "./data/test");
		seal1 = new Seal(new Tuple(1, 0), new BigInteger("1"));
		seal2 = new Seal(new Tuple(2, 0), new BigInteger("2"));
		System.out.println("Cnt for seal1 " + seal1.getCnt(null));
		System.out.println("Cnt for seal2 " + seal2.getCnt(null));
	}
	
	@Test
	public void testFold() {
//		fail("Not yet implemented");
		Seal seal3 = new Seal(seal1, seal2);
		System.out.println("Cnt for seal3 " + seal3.getCnt(null));
		assertEquals(seal3.getCnt(null), new BigInteger("2"));
		Seal seal4 = new Seal(seal3, seal2);
		System.out.println("Cnt for seal4 " + seal4.getCnt(null));
		assertEquals(seal4.getCnt(null), new BigInteger("3"));
	}
	
	@Test
	public void testSecretShare() {
		Seal seal3 = new Seal(seal1, seal2);
		System.out.println("SS for seal3 " + seal3.getSecretShare(null));
		assertEquals(seal3.getSecretShare(null), new BigInteger("3"));
		Seal seal4 = new Seal(seal3, seal2);
		System.out.println("SS for seal4 " + seal4.getSecretShare(null));
		assertEquals(seal4.getSecretShare(null), new BigInteger("5"));
	}
	
	@Test
	public void testDig() {
		seal1 = new Seal(new Tuple(128, 0), new BigInteger("1"));
		seal2 = new Seal(new Tuple(64, 0), new BigInteger("2"));
		Seal seal3 = new Seal(seal1, seal2);
		int[] comPre = Utility.comPre(new int[]{128, 8}, new int[]{64, 8}, 4);
		BigInteger dig = Utility.getBI(Hasher.hashBytes(new Integer(comPre[0]).toString().getBytes())).multiply(new BigInteger("2"));
		assertEquals(dig, seal3.getDig(null, comPre[1]));
	}
	
	@Test
	public void testLargeFolding() {
		int num = 20;
		Seal[] seals = new Seal[num];
		for (int i = 0; i < num; i ++) {
			seals[i] = new Seal(new Tuple(i, 0), new BigInteger(new Integer(i).toString()));
		}
		timer.reset();
		Seal sealA = seals[0];
		for (int i = 1; i < num; i ++) {
			sealA = new Seal(sealA, seals[i]);
		}
		timer.stop();
		System.out.println("Time consumes: " + timer.timeElapseinMs() + " ms");
		System.out.println(sealA.getCnt(null));
	}
	
	@Test
	public void testBinaryTree() {
		int num = 20;
		Seal[] seals = new Seal[num];
		for (int i = 0; i < num; i ++) {
			seals[i] = new Seal(new Tuple(i, 0), new BigInteger(new Integer(i).toString()));
		}
		timer.reset();
		while(num > 1) {
			int newNum = 0;
			for (int i = 0; i < num; i += 2) {
				if (i + 1 == num) {
					seals[newNum ++] = seals[i];
				} else {
					Seal l = seals[i];
					Seal r = seals[i + 1];
					seals[newNum ++] = new Seal(l, r);
				}
			}
			num = newNum;
		}
		timer.stop();
		System.out.println("Time consumes: " + timer.timeElapseinMs() + " ms");
		System.out.println(seals[0]);
	}

}
