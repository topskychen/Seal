/**
 * 
 */
package party;

import io.IO;
import io.RW;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;

import utility.EncFun;
import utility.EncFun.ENC_TYPE;
import utility.Global;
import utility.Utility;
import crypto.AES;

/**
 * @author chenqian
 *
 */
public class TrustedRegister {

	public static byte[] sk = null;
	public static EncFun encFun = null;
	public static ENC_TYPE type;
	public static BigInteger mod = BigInteger.ONE.shiftLeft(184 * 8 + 24 + 128 + 24);
//	public static HashMap<Integer, BigInteger> secretShares = new HashMap<Integer, BigInteger>();
	public static HashMap<Integer, BigInteger> totalSS =  new HashMap<Integer, BigInteger>();

	/**
	 * Generate Secret Share
	 * @param id
	 * @param value
	 * @return
	 */
	public static BigInteger genSecretShare(RW value) {
		return Utility.getBI(AES.encrypt(sk, IO.toBytes(value))).and(Global.BITS128);
	}
	
	public static BigInteger genSecretShare(int runId) {
		return Utility.getBI(AES.encrypt(sk, new Integer(runId).toString().getBytes())).and(Global.BITS128);
	}
	
	public static void specifyEncFun(ENC_TYPE type, String fileName) {
		TrustedRegister.type = type;
		File file = new File(fileName + "." + type);
		if (file.exists()) {
			TrustedRegister.encFun = new EncFun(file);
		} else {
			TrustedRegister.encFun = new EncFun(type, mod);
			IO.toFile(TrustedRegister.encFun, file);
		}
	}
	
//	public static void addSecretShare(int id, BigInteger secretShare) {
//		secretShares.put(id, secretShare);
//	}
//	
//	public static BigInteger getSecretShare(int id) {
//		return secretShares.get(id);
//	}
	
	/**
	 * Construct a trustedRegister with the type.
	 * @param type
	 */
	public TrustedRegister() {
		// TODO Auto-generated constructor s
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
