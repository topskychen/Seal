/**
 * 
 */
package test;

import static org.junit.Assert.*;
import index.SearchIndex.INDEX_TYPE;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import crypto.AES;
import party.TrustedRegister;
import spatialindex.Point;
import utility.Tuple;
import utility.EncFun.ENC_TYPE;

/**
 * @author chenqian
 *
 */
public class TestTrustRegister {

	@BeforeClass
	public static void init() {
		TrustedRegister.sk = AES.getSampleKey();
	}
	
	@Test
	public void test() {
		System.out.println(TrustedRegister.genSecretShare(new Tuple(1, new Point(new double[] {1}), 0, null, INDEX_TYPE.BTree)).bitLength());
	}

}
