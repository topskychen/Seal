/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;
import index.Entry;

import java.math.BigInteger;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import party.TrustedRegister;
import spatialindex.Point;
import utility.EncFun.ENC_TYPE;
import utility.Tuple;

/**
 * @author chenqian
 *
 */
public class TestEntry {

	static TrustedRegister tr = null;
	static ArrayList<Entry> entries = null;
	static ArrayList<Tuple> tuples = null;
	
	@BeforeClass
	public static void init() {
		tr = TrustedRegister.getInstance(ENC_TYPE.Paillier, "./data/test");
		int num = 32;
		entries = new ArrayList<Entry>();
		tuples = new ArrayList<Tuple>();
		for (int i = 0; i < num; i ++) {
			tuples.add(new Tuple(i, new Point(new double[] {i}), 0, null));
			entries.add(new Entry(tuples.get(i), null));
			BigInteger ss = tr.genSecretShare(tuples.get(i));
//			System.out.println(ss);
			entries.get(i).prepareSeal(ss);
//			System.out.println(entries.get(i).getSeal().getSecretShare(null));
		}
		
	}
	
	@Test
	public void testSS() {
		Entry e1 = entries.get(0);
		Entry e2 = entries.get(1);
		Entry e3 = new Entry(e1, e2, -1);
		BigInteger ss1 = tr.genSecretShare(tuples.get(0));
		BigInteger ss2 = tr.genSecretShare(tuples.get(1));
		System.out.println(ss1.add(ss2));
		System.out.println(e3.getSeal().getSecretShare(null));
		assertEquals(ss1.add(ss2), e3.getSeal().getSecretShare(null));
	}
	
	public Entry buildTree() {
		int num = entries.size();
		Entry[] entries2 = new Entry[num];
		for (int i = 0; i < num; i ++) {
			entries2[i] = entries.get(i);
		}
		while(num > 1) {
			int newNum = 0;
			for (int i = 0; i < num; i += 2) {
				if (i + 1 == num) {
					entries2[newNum ++] = entries2[i];
				} else {
					Entry l = entries2[i];
					Entry r = entries2[i + 1];
					entries2[newNum ++] = new Entry(l, r, -1);
				}
			}
			num = newNum;
		}
		return entries2[0];
	}
	
	@Test
	public void testMerge() {
		Entry entry = buildTree();
		System.out.println(entry);
	}
	
	@Test
	public void testSecretShare() {
		BigInteger ps = BigInteger.ZERO;
		for (int i = 0; i < tuples.size(); i ++) {
			BigInteger secretShare = tr.genSecretShare(tuples.get(i));
			ps = ps.add(secretShare);
		}
		Entry entry = buildTree();
		BigInteger rs = entry.getSeal().getSecretShare(null);
//		System.out.println(ps);
//		System.out.println(rs);
		assertEquals(ps, rs);
	}

}
