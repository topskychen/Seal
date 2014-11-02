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

	private byte[] sk = null;
	private EncFun encFun = null;
	private ENC_TYPE type;
	private BigInteger mod = BigInteger.ONE.shiftLeft(184 * Global.L + 24 + 128 + 24); // seal content length
	private HashMap<Integer, BigInteger> totalSecrets = new HashMap<Integer, BigInteger>();

	static TrustedRegister instance = null;
	
	public void putTotalSecret(int id, BigInteger secret) {
		totalSecrets.put(id, secret);
	}

	public BigInteger getTotalSS(int id) {
		return totalSecrets.get(id);
	}
	
	public BigInteger decrypt(BigInteger content, BigInteger random) {
		return encFun.decrypt(content, random);
	}
	
	public BigInteger encrypt(BigInteger content, BigInteger random) {
		return encFun.encrypt(content, random);
	}

	
	public ENC_TYPE getType() {
		return type;
	}
	
	public BigInteger getMod() {
		return mod;
	}
	
	public static TrustedRegister getInstance() {
		if (instance == null) {
			instance = new TrustedRegister(ENC_TYPE.Paillier, "./data/test");
		}
		return instance;
	}
	
	public static TrustedRegister getInstance(ENC_TYPE type, String fileName) {
		if (instance == null) {
			instance = new TrustedRegister(type, fileName);
		}
		return instance;
	}
	
	
	/**
	 * Generate Secret Share
	 * @param id
	 * @param value
	 * @return
	 */
	public BigInteger genSecretShare(RW value) {
		return Utility.getBI(AES.encrypt(sk, IO.toBytes(value))).and(Global.BITS128);
	}
	
	public BigInteger genSecretShare(int runId) {
		return Utility.getBI(AES.encrypt(sk, new Integer(runId).toString().getBytes())).and(Global.BITS128);
	}
	
	/**
	 * Construct a trustedRegister with the type.
	 * @param type
	 */
	TrustedRegister(ENC_TYPE type, String fileName) {
		this.sk = AES.getSampleKey();
		this.type = type;
		File file = new File(fileName + "." + type);
		if (file.exists()) {
			encFun = new EncFun(file);
		} else {
			encFun = new EncFun(type, mod);
			IO.toFile(encFun, file);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
}
