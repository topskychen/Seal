/**
 * 
 */
package test;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import rtree.RTree;
import spatialindex.IData;
import spatialindex.INode;
import spatialindex.ISpatialIndex;
import spatialindex.IVisitor;
import spatialindex.Region;
import storagemanager.IBuffer;
import storagemanager.IStorageManager;
import storagemanager.MemoryStorageManager;
import storagemanager.PropertySet;
import storagemanager.RandomEvictionsBuffer;

/**
 * @author chenqian
 *
 */
public class TestMemRTree {

	static ISpatialIndex tree = null;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		IStorageManager sm = new MemoryStorageManager();
		IBuffer buffer = new RandomEvictionsBuffer(sm, 10, false);
		PropertySet ps = new PropertySet();
		ps.setProperty("FillFactor", new Double(0.7));
		ps.setProperty("IndexCapacity", new Integer(100));
		ps.setProperty("LeafCapacity", new Integer(100));
		ps.setProperty("Dimension", new Integer(2));
		tree = new RTree(ps, buffer);
	}

	@Test
	public void testInsert() {
		double[] f1 = new double[]{1, 1};
		double[] f2 = new double[]{4, 4};
		Region r = new Region(f1, f2);
		tree.insertData(null, r, 0);
//		System.out.println(tree);
		testRangeQuery();
	}
	
	public void testRangeQuery() {
		double[] f1 = new double[]{0, 0};
		double[] f2 = new double[]{5, 5};
		Region r = new Region(f1, f2);
		MyVisitor vis = new MyVisitor();
		tree.intersectionQuery(r, vis);
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
