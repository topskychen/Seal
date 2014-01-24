/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import index.Entry;
import index.MemQTree;
import io.P;

import org.junit.BeforeClass;
import org.junit.Test;

import spatialindex.Point;
import utility.Constants;

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
		MemQTree qtree = Constants.G_QTREE;
		ArrayList<Integer> ids = qtree.getPath(new Point(new double[]{120, 232}));
		P.Print(ids);
		ids = qtree.getPath(new Point(new double[]{1234, 8323}));
		P.Print(ids);
	}

}
