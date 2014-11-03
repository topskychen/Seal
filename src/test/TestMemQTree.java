/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import index.Entry;
import index.MemQTree;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

import memoryindex.QuadEntry;

import org.junit.BeforeClass;
import org.junit.Test;

import spatialindex.Point;
import utility.Seal;
import utility.Sim;
import utility.StatisticsUpdate;

/**
 * @author chenqian
 *
 */
public class TestMemQTree {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@Test
	public void testGetPath() {
		Sim sim = new Sim();
		MemQTree qtree = MemQTree.createTree(sim);
		int[] ids = qtree.getPrefix(new Point(new double[]{120, 232, 2}));
		for (int id : ids) System.out.print(id + " "); System.out.println();
		ids = qtree.getPrefix(new Point(new double[]{1234, 8323, 7}));
		for (int id : ids) System.out.print(id + " "); System.out.println();
	}
	
	@Test
	public void testGQtree() {
		Random rand = new Random();
		Sim sim = new Sim();
		MemQTree qtree = MemQTree.createTree(sim);
		ArrayList<Entry> entries = new ArrayList<>();
		BigInteger secret = BigInteger.ZERO;
		for (int i = 0; i < 100; ++i) {
			double[] coords = new double[sim.getDim()];
			for (int j = 0; j < sim.getDim(); ++j) {
				coords[j] = rand.nextDouble();
			}
			Point point = new Point(coords);
			Entry entry = new Entry(i, point, 0, qtree);
			BigInteger ss = sim.getTrustedRegister().genSecretShare(0);
			entry.prepareSeal(ss);
			entries.add(entry);
			secret = secret.add(ss);
		}
		qtree.updateIndex(entries, new StatisticsUpdate());
		assertTrue(qtree.checkCount());
		assertTrue(qtree.checkTree());
		ArrayList<Seal> seals = new ArrayList<Seal>();
		for (QuadEntry entry : qtree.getEntries()) {
			if (entry != null) {
				seals.add(((Entry) entry).getSeal());
			}
		}
		Seal seal = new Seal(seals.toArray(new Seal[0]));
		assertEquals(seal.getSecretShare(null), secret);
	}

}