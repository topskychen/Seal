/**
 * 
 */
package utility;

import java.math.BigInteger;

import crypto.Constants;
import crypto.Paillier;
import party.ServiceProvider;

/**
 * @author chenqian
 *
 */
public class EncFun {

	public static enum ENC_TYPE {Paillier, OTPad};
	
	private ENC_TYPE type;
	private BigInteger mod;
	public static Paillier paillier;
	private BigInteger kInv; 
	
	/**
	 * Encrypt a content according to the type.
	 * The type is specified when constructor is called.
	 * @param content
	 * @param random
	 * 			when paillier, the random is the randomized parameter
	 * 			when one-time pad, the random is the one-time pad.
	 * @return
	 */
	public BigInteger encrypt(BigInteger content, BigInteger random) {
		BigInteger cipher = null;
		if (type == ENC_TYPE.Paillier) {
			cipher = paillier.Encrypt(content, random);
		} else {
			cipher = (Constants.PRIME_Q.multiply(content)).add(random).mod(mod);
		}
		return cipher;
	}
	
	/**
	 * Decrypt a encrypted content.
	 * @param cipher
	 * @param random
	 * 			when paillier, the random is null
	 * 			when one-time pad, the random is the (sum of) one-time pad
	 * @return
	 */
	public BigInteger decrypt(BigInteger cipher, BigInteger random) {
		BigInteger content = null;
		if (type == ENC_TYPE.Paillier) {
			content = paillier.Decrypt(cipher).mod(paillier.n).mod(mod);
		} else {
			content = cipher.subtract(random);
			if (content.signum() < 0 ){
				content = content.add(mod);
			}
			content = kInv.multiply(content).mod(mod);
		}
		return content;
	}
	
	/**
	 * Constructor, needs specifications of type and modulus.
	 */
	public EncFun(ENC_TYPE type, BigInteger mod) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.mod = mod;
		if (type == ENC_TYPE.Paillier) {
			paillier = new Paillier(mod.bitLength() + 1, 100);
			while(paillier.n.compareTo(mod) < 0) {
				paillier = new Paillier(mod.bitLength() + 1, 100);
			}
		} else {
			kInv = Constants.PRIME_Q.modInverse(mod);
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}