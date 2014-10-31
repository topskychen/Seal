/**
 * 
 */
package test;

import index.MemQTree;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import spatialindex.Point;
import utility.Global;

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
		MemQTree qtree = Global.G_QTREE;
		List<Integer> ids = qtree.getPath(new Point(new double[]{120, 232}));
//		P.Print(ids);
		ids = qtree.getPath(new Point(new double[]{1234, 8323}));
//		P.Print(ids);
	}

}