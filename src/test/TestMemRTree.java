/**
 * 
 */
package test;

import index.MemRTree;

import org.junit.BeforeClass;
import org.junit.Test;

import spatialindex.IData;
import spatialindex.INode;
import spatialindex.IVisitor;
import spatialindex.Point;
import spatialindex.Region;
import utility.Sim;

/**
 * @author chenqian
 *
 */
public class TestMemRTree {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@Test
	public void testInsert() {
		Sim sim = new Sim();
		MemRTree memRtree = MemRTree.createTree(sim);
		double[] f1 = new double[]{1, 1, 1};
		double[] f2 = new double[]{4, 4, 4};
		Point p1 = new Point(f1);
		Point p2 = new Point(f2);
		memRtree.insertData(null, p1, 0);
		memRtree.insertData(null, p2, 1);
//		System.out.println(tree);
		testRangeQuery(memRtree);
	}
	
	public void testRangeQuery(MemRTree memRTree) {
		double[] f1 = new double[]{0, 0, 0};
		double[] f2 = new double[]{5, 5, 5};
		Region r = new Region(f1, f2);
		MyVisitor vis = new MyVisitor();
		memRTree.intersectionQuery(r, vis);
	}
	
	@Test
	public void testGetPrefix() {
		Sim sim = new Sim();
		MemRTree memRtree = MemRTree.createTree(sim);
		double[] f1 = new double[]{1, 1, 1};
		double[] f2 = new double[]{4, 4, 4};
		Point p1 = new Point(f1);
		Point p2 = new Point(f2);
		memRtree.insertData(null, p1, 0);
		memRtree.insertData(null, p2, 1);
		int[] prefix = memRtree.getPrefix(p1);
		for (int p : prefix) System.out.print(p + " "); System.out.println();
		prefix = memRtree.getPrefix(p2);
		for (int p : prefix) System.out.print(p + " "); System.out.println(); 
	}
	
	class MyVisitor implements IVisitor
	{
		public void visitNode(final INode n) {}

		public void visitData(final IData d)
		{
			System.out.println(d.getIdentifier());
				// the ID of this data entry is an answer to the query. I will just print it to stdout.
		}
	}
}
