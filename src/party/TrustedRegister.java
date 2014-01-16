/**
 * 
 */
package party;

import java.math.BigInteger;
import java.util.HashMap;

import io.IO;
import io.RW;
import crypto.AES;
import sun.org.mozilla.javascript.internal.ast.NewExpression;
import utility.EncFun;
import utility.EncFun.ENC_TYPE;

/**
 * @author chenqian
 *
 */
public class TrustedRegister {

	private static byte[] sk = null;
	public static EncFun encFun = null;
	public static ENC_TYPE type;
	public static BigInteger mod = BigInteger.ONE.shiftLeft(184 * 8 + 128 + 24);
	public static HashMap<Integer, byte[]> secretShares = new HashMap<>();
	
	/**
	 * Generate Secret Share
	 * @param id
	 * @param value
	 * @return
	 */
	public static byte[] genSecretShare(int id, RW value) {
		byte[] content = IO.concat(new Integer(id).toString().getBytes(), IO.toBytes(value));
		return AES.encrypt(sk, content);
	}
	
	public static void specifyEncFun(ENC_TYPE type) {
		TrustedRegister.type = type;
		TrustedRegister.encFun = new EncFun(type, mod);
	}
	
	public static void addSecretShare(int id, byte[] secretShare) {
		secretShares.put(id, secretShare);
	}
	
	public static byte[] getSecretShare(int id) {
		return secretShares.get(id);
	}
	
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
