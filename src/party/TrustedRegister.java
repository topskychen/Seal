/**
 * 
 */
package party;

import java.math.BigInteger;

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

	private byte[] sk = null;
	private EncFun encFun = null;
	public static ENC_TYPE type;
	public static BigInteger mod = BigInteger.ONE.shiftLeft(184 * 8 + 128 + 24);
	
	/**
	 * Generate Secret Share
	 * @param id
	 * @param value
	 * @return
	 */
	public byte[] getSecretShare(int id, RW value) {
		byte[] content = IO.concat(new Integer(id).toString().getBytes(), IO.toBytes(value));
		return AES.encrypt(sk, content);
	}
	
	public EncFun getEncFun () {
		return encFun;
	}
	
	/**
	 * Construct a trustedRegister with the type.
	 * @param type
	 */
	public TrustedRegister(ENC_TYPE type) {
		// TODO Auto-generated constructor s
		this.type = type;
		this.encFun = new EncFun(type, mod);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
