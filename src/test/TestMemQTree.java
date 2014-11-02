/**
 * 
 */
package test;

import index.MemQTree;

import org.junit.BeforeClass;
import org.junit.Test;

import spatialindex.Point;
import utility.Sim;

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
		int[] ids = qtree.getPrefix(new Point(new double[]{120, 232}));
		for (int id : ids) System.out.print(id + " "); System.out.println();
		ids = qtree.getPrefix(new Point(new double[]{1234, 8323}));
		for (int id : ids) System.out.print(id + " "); System.out.println();
	}

}