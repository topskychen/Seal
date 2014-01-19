/**
 * 
 */
package test;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import crypto.Constants;
import crypto.Paillier;
import utility.EncFun;
import utility.EncFun.ENC_TYPE;

/**
 * @author chenqian
 *
 */
public class TestEncFun {

	EncFun encFunP;
	EncFun encFunO;
	BigInteger mod;
	@BeforeClass
	public void init() {
		mod = new BigInteger("65536");
		encFunP = new EncFun(ENC_TYPE.Paillier, mod);
		encFunO = new EncFun(ENC_TYPE.OTPad, mod);
	}
	
	@Test
	public void testFoldPaillier() {
		BigInteger b1 = new BigInteger("491511231");
		BigInteger b2 = new BigInteger("491511233");
		BigInteger c1 = encFunP.encrypt(b1, Constants.PRIME_P);
		BigInteger c2 = encFunP.encrypt(b2, Constants.PRIME_P);
		BigInteger c3 = c1.multiply(c2).mod(encFunP.paillier.nsquare);
		BigInteger m3 = encFunP.decrypt(c3, null);
		assertEquals(b1.add(b2).mod(encFunP.paillier.n).mod(mod), m3);
	}
	
	@Test
	public void testFoldOTPad() {
		BigInteger b1 = new BigInteger("491511231");
		BigInteger b2 = new BigInteger("491511233");
		BigInteger c1 = encFunO.encrypt(b1, Constants.PRIME_P);
		BigInteger c2 = encFunO.encrypt(b2, Constants.PRIME_P);
		BigInteger c3 = c1.add(c2).mod(mod);
		BigInteger m3 = encFunO.decrypt(c3, Constants.PRIME_P.add(Constants.PRIME_P));
		assertEquals(b1.add(b2).mod(mod), m3);
	}

}
